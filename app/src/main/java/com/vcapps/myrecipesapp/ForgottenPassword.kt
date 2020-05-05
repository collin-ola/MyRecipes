package com.vcapps.myrecipesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_forgotten_password.*

class ForgottenPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(R.layout.activity_forgotten_password)

        resetPassword.setOnClickListener {

            resetPWButton(emailTextView.text.toString())
        }
    }

    private fun resetPWButton(userEmailText: String) {
        if (userEmailText.trim() == "") {
            makeToast("Please enter your details.")
            return
        }
        //Create an instance of Firebase database
        val db = Firebase.firestore

        db.collection("myRecipeUsers").document(userEmailText)
            .get()
            .addOnSuccessListener { document ->

                if (document.data != null) {
                    val userEmailStored = document["emailAddress"].toString();
                    makeToast("Email: $userEmailStored")

                } else {
                    makeToast("This user does not exist, Please register")
                }
            }

            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)

            }
    }
    fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
}
