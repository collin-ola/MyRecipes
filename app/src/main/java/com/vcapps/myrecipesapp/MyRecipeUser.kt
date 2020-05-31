package com.vcapps.myrecipesapp

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MyRecipeUser(var _username: String?, var _eMailAddress: String?, var _password: String?, var _confirmPassword: String?, var _photoUrl: Uri? = null) {
    
    var username = _username.toString()
    var emailAddress = _eMailAddress.toString()
    var password = _password.toString()
    var confirmPassword = _confirmPassword.toString()
    var photoUrl = _photoUrl.toString()

    private lateinit var auth: FirebaseAuth

    fun registerFacebookUser(accountType: String) { //types standard, google, facebook
        //Initialize database
        val db = Firebase.firestore
        val user: HashMap<String, String> = hashMapOf(
                    "name" to username,
                    "emailAddress" to emailAddress,
                    "photo" to photoUrl
                )
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

