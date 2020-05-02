package com.vcapps.myrecipesapp

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MyRecipeUser (var _name: String?, var _username: String?, var _eMailAddress: String?, var _password: String?, var _confirmPassword: String?) {
    var name = _name
    var username = _username
    var emailAddress = _eMailAddress
    var password = _password
    var confirmPassword = _confirmPassword


    fun registerUser(type: String){ //types standard, google, facebook
        //Initialize database
        val db = Firebase.firestore
        var usr : HashMap<String, String>? = null

        val user = hashMapOf(
            "name" to name,
            "username" to username,
            "emailAddress" to emailAddress,
            "password" to password
        )

        when (type) {
            "google" -> {
                MainActivity().makeToast("Google log in!")
            }

            "facebook" -> {
                MainActivity().makeToast("Facebook log in!")
            }

            "standard" -> {

            }
        }
    }
}