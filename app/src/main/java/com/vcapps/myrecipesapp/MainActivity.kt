package com.vcapps.myrecipesapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*


object RequestCodes {
    const val requestCodeOK = 0
    const val requestCodeSignIn = 2
    const val requestCodeRegister = 3
    const val requestCodeFacebook = 64206
}

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        val facebookIntent = Intent(this, ProfileActivity::class.java)
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLogin)
        facebookLoginButton.setPermissions(listOf("email","public_profile"))
        facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    val request = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, _ ->
                        // Application code
                        val name = `object`.getString("name")
                        val email = `object`.getString("email")
                        val img = `object`.getJSONObject("picture").getJSONObject("data").getString("url")

                        //val fbUser = MyRecipeUser(name,email,"","", Uri.parse(img))
                        //fbUser.registerFacebookUser("facebook")
                        handleFacebookAccessToken(loginResult.accessToken)
                    }

                    startActivity(facebookIntent)

                    val parameters = Bundle()
                    parameters.putString("fields", "id, name, email, picture.type(large)")
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException?) {
                    Log.d(TAG, "facebook:onError", error)
                }

            })

        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(R.layout.activity_main)


//*********************** Login Button onclick Listners *****************************
        loginButton.setOnClickListener {
            val userEmailText = emailTextView.text.toString()
            val userPasswordText = passwordTextView.text.toString()

            loginButton(userEmailText, userPasswordText)
        }

        googleLogin.setOnClickListener {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.wcid))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleSignInClient.signInIntent, RequestCodes.requestCodeSignIn)
        }

        registerButton.setOnClickListener {
           val intent = Intent(this, RegistrationActivity::class.java)
           startActivityForResult(intent, RequestCodes.requestCodeRegister)
        }

        forgottenPassword.setOnClickListener {
            val intent = Intent(this, ForgottenPassword::class.java)
            startActivity(intent)
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken?) {
        Log.d(TAG, "handleFacebookAccessToken:$token")

        val credential = FacebookAuthProvider.getCredential(token!!.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    makeToast("Facebook login successful.")
                        //val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    makeToast("Authentication failed.")
                }
            }
    }


//********************* User Login **********************************

   private fun loginButton(userEmailText: String, userPasswordText: String) {
       if (userEmailText.trim() == "" || userPasswordText.trim() == "") {

           makeToast("Please enter your details.")
           return
       }

       auth.signInWithEmailAndPassword(userEmailText, userPasswordText)
           .addOnCompleteListener(this) { task ->
               if (task.isSuccessful) {
                   // Sign in success, update UI with the signed-in user's information
                   Log.d(TAG, "signInWithEmail:success")
                   makeToast("Logged in. Welcome $userEmailText")
                   val intent = Intent(this, ProfileActivity::class.java)
                   intent.putExtra("userEmailAddress", userEmailText)
                   startActivity(intent)

               } else {
                   // If sign in fails, display a message to the user.
                   makeToast("Login failed. Try again")
                   Log.w(TAG, "signInWithEmail:failure", task.exception)
               }
           }
           .addOnFailureListener(this) {
               makeToast("Error: ${it.message}")
           }

       //******************* User settings ***********************
/*
       val user = FirebaseAuth.getInstance().currentUser

       user!!.updateEmail(userEmailText)
           .addOnCompleteListener{task ->
               if (task.isSuccessful) {
                   Log.d(TAG, "User email address updated.")
               }
           }
       user!!.updatePassword(userPasswordText)
           .addOnCompleteListener{task ->
               if (task.isSuccessful) {
                   Log.d(TAG, "User password updated.")
                   startActivity(intent)
               }
           }*/
   }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.v(TAG, "In OAR...")

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RequestCodes.requestCodeSignIn) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e)
            }
        } else if (requestCode == RequestCodes.requestCodeRegister) {
            Log.d(TAG, "Returned with e-mail address. Code is $requestCode")
            if (resultCode == RequestCodes.requestCodeOK && data != null)
                data.apply {
                    val email = getStringExtra("email")
                    emailTextView.setText(email)
                }
        } else if (requestCode == RequestCodes.requestCodeFacebook) {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
    }

    //**************** Google login ***********************

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    Log.d(TAG, "User details: ${user?.displayName}")
                    Log.d(TAG, "User details: ${user?.email}")
                    Log.d(TAG, "User details: ${user?.photoUrl}")

                    makeToast("Login Successful")


                    if (user != null) {
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("userEmailAddress", user.email)
                        startActivity(intent)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    makeToast("Authentication has failed - please try again.")
                }
            }
    }

    //Check to see if a user is already logged in via google. If so, log them in automatically
    private fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()

}


