package com.vcapps.myrecipesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

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
    val loginManager: LoginManager = LoginManager.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        callbackManager = CallbackManager.Factory.create()

        val facebookIntent = Intent(this, RegistrationActivity::class.java)
        val facebookLoginButton = findViewById<LoginButton>(R.id.facebookLogin)
        facebookLoginButton.setPermissions("email", "name", "public_profile")
        facebookLoginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

            override fun onSuccess(loginResult: LoginResult) {
                makeToast("Facebook Login Successful")
                Log.d(TAG, "facebook:onSuccess:${loginResult}")
                startActivity(facebookIntent)
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

        loginButton.setOnClickListener {
            val userEmailText = emailTextView.text.toString()
            val userPasswordText = passwordTextView.text.toString()

            loginButton(userEmailText,userPasswordText)
        }

        googleLogin.setOnClickListener{
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.wcid))
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            startActivityForResult(googleSignInClient.signInIntent, RequestCodes.requestCodeSignIn)
        }

        registerButton.setOnClickListener {
           val intent = Intent(this, RegistrationActivity::class.java)
            //startActivity(intent)
            startActivityForResult(intent, RequestCodes.requestCodeRegister)
        }

        forgottenPassword.setOnClickListener {
            val intent = Intent(this, ForgottenPassword::class.java)
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun loginButton(userEmailText:String, userPasswordText:String){
        if(userEmailText.trim() == "" || userPasswordText.trim() == ""){
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

                        if(checkPassword(userPasswordText, passwordStored)) {
                            makeToast("Logged in. Welcome $userEmailStored")
                            /*val intent = Intent(this, ForgottenPassword::class.java)
                            startActivity(intent)*/
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

    private fun loginGoogle(mru: MyRecipeUser) {
        //Create an instance of Firebase database
        val db = Firebase.firestore

        if (mru.emailAddress != null) {
            db.collection("myRecipeUsers").document(mru.emailAddress!!)
                .get()
                .addOnSuccessListener { document ->
                    if (document.data != null) {
                        val userEmailStored = document["emailAddress"].toString();
                        //TODO: Take user to their home page

                        makeToast("User $userEmailStored will now be taken to their home page")

                        /*val intent = Intent(this, ForgottenPassword::class.java)
                        startActivity(intent)*/
                    } else {
                        //TODO - Will hit this condition if user doesn't exist, so create an account for them.
                        makeToast("Entered e-mail address: ${mru.emailAddress.toString()} Need to create an account")
                        registerGoogle(mru)
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }
        }
    }

    private fun registerGoogle(mru: MyRecipeUser) {
        val db = Firebase.firestore

        val user = hashMapOf(
            "name" to mru.name,
            "username" to "",
            "emailAddress" to mru.emailAddress,
            "password" to ""
        )

        db.collection("myRecipeUsers").document(mru.emailAddress!!)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Document has been added")
                makeToast("Google user registration complete. Welcome ${mru.name}!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
                makeToast("There has a registration error. Please re-install the app and try again.")
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

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
            if(resultCode == RequestCodes.requestCodeOK && data != null)
                data.apply {
                    val email = getStringExtra("email")
                    emailTextView.setText(email)
                }
        } else if (requestCode == RequestCodes.requestCodeFacebook) {
            TODO("Uncomment the below")
            //callbackManager.onActivityResult(requestCode, resultCode, data)
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


                    val mru = MyRecipeUser(user?.displayName,"", user?.email,"","" )
                        //updateUI(user)
                    if (user != null) {
                        loginGoogle(mru)
                    }
                        makeToast("Authentication is successful. Welcome ${user?.displayName}")
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    makeToast("Authentication has failed - please try again.")
                }
            }
    }

    private fun checkPassword(userPw: String, dbPw: String) :Boolean = userPw==dbPw

    //Check to see if a user is already logged in via google. If so, log them in automatically
    /*override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(HomeActivity.getLaunchIntent(this))
            finish()
        }
    }*/

    fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
}

