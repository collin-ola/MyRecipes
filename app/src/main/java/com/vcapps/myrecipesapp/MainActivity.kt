package com.vcapps.myrecipesapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        loginButton.setOnClickListener {
            val userNameText = usernameTextView.text.toString()
            val userPasswordText = passwordTextView.text.toString()

            loginButton(userNameText,userPasswordText)
        }

        registerButton.setOnClickListener {

           val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
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

    fun loginButton(userNameText:String, userPasswordText:String){

        if(userNameText.trim() == "" || userPasswordText.trim() == ""){
            makeToast("Please enter your details.")

        return
        }
            //Create an instance of Firebase database
            val db = Firebase.firestore

            db.collection("myRecipeUsers").document(userNameText)
                .get()
                .addOnSuccessListener { document ->

                    if (document.data != null) {
                        val passwordStored = document["password"].toString()
                        val usernameStored = document["username"].toString();

                        if(checkPassword(userPasswordText, passwordStored)) {
                            makeToast("Logged in. Welcome $usernameStored")
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


    private fun checkPassword(userPw: String, dbPw: String) :Boolean = userPw==dbPw

    private fun makeToast(toastText :String){
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
    }

}

