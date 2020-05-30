package com.vcapps.myrecipesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*

//Global tag used for logging
const val TAG = "MRA"

class RegistrationActivity : AppCompatActivity() {

    private var name: String? = null
    private var username: String? = null
    private var emailAddress: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(R.layout.activity_registration)

        submitButton.setOnClickListener {

            name = nameEditText.text.toString()
            username = usernameEditText.text.toString()
            emailAddress = emailEditText.text.toString()
            password = pwEditText.text.toString()
            confirmPassword = confirmPwEditText.text.toString()

            val mru =
                MyRecipeUser(name!!, username!!, emailAddress!!, password!!, confirmPassword!!)

            if (validate(mru)) {
                createAccount(emailAddress!!, password!!)
                returnResult(mru.emailAddress)
                makeToast("User registration complete.")
            }
        }
    }

    private fun makeToast(toastText: String) {
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
    }

    private fun validate(user: MyRecipeUser): Boolean {
        return if (user.password != user.confirmPassword) {
            makeToast("Your passwords do not match  - please check and try again.")
            false
        } else {
            true
        }
    }

    private fun returnResult(emailAddress: String) {
        val data = Intent().apply {
            putExtra("email", emailAddress)
        }
        setResult(RequestCodes.requestCodeOK, data)
        finish()
    }

   private fun createAccount(emailAddress: String, password: String) {
        var auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(emailAddress, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)

                }
        }
    }
}