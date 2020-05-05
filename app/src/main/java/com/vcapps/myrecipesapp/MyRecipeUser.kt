package com.vcapps.myrecipesapp

import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyRecipeUser(var _name: String?, var _username: String?, var _eMailAddress: String?, var _password: String?, var _confirmPassword: String?, var _photoUrl: Uri? = null) {

    var name = _name.toString()
    var username = _username.toString()
    var emailAddress = _eMailAddress.toString()
    var password = _password.toString()
    var confirmPassword = _confirmPassword.toString()
    var photoUrl = _photoUrl.toString()


    fun registerUser(accountType: String) { //types standard, google, facebook
        //Initialize database
        val db = Firebase.firestore
        lateinit var user : HashMap<String, String>

        when (accountType) {
            "google" -> {
                user = hashMapOf(
                    "name" to name,
                    "emailAddress" to emailAddress,
                    "photo" to photoUrl
                )
            }

            "facebook" -> {
                user = hashMapOf(
                    "name" to name,
                    "emailAddress" to emailAddress
                )
            }

            "standard" -> {
                user = hashMapOf(
                    "name" to name,
                    "username" to username,
                    "emailAddress" to emailAddress,
                    "password" to password
                    )
            }
        }

        db.collection("myRecipeUsers").document(emailAddress)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Document has been added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
            }
    }
}