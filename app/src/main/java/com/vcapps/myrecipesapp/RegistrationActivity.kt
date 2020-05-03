package com.vcapps.myrecipesapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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

            val mru = MyRecipeUser(name!!, username!!, emailAddress!!, password!!, confirmPassword!!)

            if(validate(mru))
                mru.registerUser("standard")
                //register(mru)
        }
    }

    private fun makeToast(toastText: String){
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
    }

    private fun validate(user: MyRecipeUser): Boolean {
        return if(user.password != user.confirmPassword) {
            makeToast("Your passwords do not match  - please check and try again.")
            false
        } else {
            true
        }
    }

    private fun register(mrUser: MyRecipeUser) {
        val db = Firebase.firestore

        val user = hashMapOf(
            "name" to mrUser.name,
            "username" to mrUser.username,
            "emailAddress" to mrUser.emailAddress,
            "password" to mrUser.password
        )

        db.collection("myRecipeUsers").document(mrUser.emailAddress!!)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Document has been added")
                makeToast("Registration complete. Welcome ${mrUser.name}!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
                makeToast("There has a registration error. Please re-install the app and try again.")
            }

        returnResult(mrUser.emailAddress!!)
    }

    private fun returnResult(emailAddress :String) {
        val data = Intent().apply {
            putExtra("email", emailAddress)
        }
        setResult(RequestCodes.requestCodeOK, data)
        finish()
    }
}