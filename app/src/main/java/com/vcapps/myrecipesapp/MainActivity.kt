package com.vcapps.myrecipesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    fun loginButton(userNameText:String, userPasswordText:String){

        if(userNameText.trim() == "" || userPasswordText.trim() == ""){
            makeToast("Please enter your details.")

        return
        }

        val TAG = "COLLINS"

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

