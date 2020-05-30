package com.vcapps.myrecipesapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*


object RequestCodes {
    const val requestCodeOK = 0
    //const val requestCodeNotOK = 1
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

                        val fbUser = MyRecipeUser(name,"",email,"","", Uri.parse(img))
                        fbUser.registerUser("facebook")
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

        auth = FirebaseAuth.getInstance()

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

//********************* Facebook User Login **********************************

    private fun loginButton(userEmailText: String, userPasswordText: String) {
        if (userEmailText.trim() == "" || userPasswordText.trim() == "") {

            makeToast("Please enter your details.")
            return
        }
        //Create an instance of Firebase database
        val db = Firebase.firestore

        db.collection("myRecipeUsers").document(userEmailText)
            .get()
            .addOnSuccessListener { document ->

                if (document.data != null) {
                    val passwordStored = document["password"].toString()
                    val userEmailStored = document["emailAddress"].toString();
                  
                    if (checkPassword(userPasswordText, passwordStored)) {
                        makeToast("Logged in. Welcome $userEmailStored")
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("userEmailAddress", userEmailStored)
                        startActivity(intent)
                    } else {
                        makeToast("Login failed. Try again")
                    }

                } else {
                    makeToast("This user does not exist, Please register")
                }
            }

            .addOnFailureListener { exception ->
             Log.w(TAG, "Error getting documents: ", exception)

            }
    }

//************************* Google login **********************************

    private fun loginGoogle(mru: MyRecipeUser) {
        //Create an instance of Firebase database
        val db = Firebase.firestore

        db.collection("myRecipeUsers").document(mru.emailAddress)
            .get()
            .addOnSuccessListener { document ->
                if (document.data != null) {
                    //val userEmailStored = document["emailAddress"].toString();
                    Log.v(TAG, "Just before intent")
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                } else {
                    //Register an account for the google user
                    mru.registerUser("google")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
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
                // ...
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


                    val mru = MyRecipeUser(user?.displayName,"", user?.email,"","", user?.photoUrl )
                    Log.v(TAG, "Created MRU")
                        //updateUI(user)
                  
                    if (user != null) {
                        Log.d(TAG, "Google login....")
                        loginGoogle(mru)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    makeToast("Authentication has failed - please try again.")
                }
            }
    }

    private fun checkPassword(userPw: String, dbPw: String): Boolean = userPw == dbPw

    /*override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(HomeActivity.getLaunchIntent(this))
            finish()
        }
    }*/

    //Check to see if a user is already logged in via google. If so, log them in automatically
    fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()

}


