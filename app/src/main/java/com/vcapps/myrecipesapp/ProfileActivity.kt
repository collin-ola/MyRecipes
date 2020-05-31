package com.vcapps.myrecipesapp

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.ListView
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    private var recipeTitle: String = "Plantain Pancakes"
    private var recipeDesc: String? = "This is a Plantain pancakes recipe I learnt from my ancestors and I really love it. I hope you like it too"
    private lateinit var recipeList: ListView
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        var userEmail: String? = intent.getStringExtra("userEmailAddress")

        makeToast("******Successful!!!!!!!******")

        val bmp = BitmapFactory.decodeResource(resources, R.drawable.profile)
        val rounded_bmp = RoundedBitmapDrawableFactory.create(resources, bmp)
        rounded_bmp.cornerRadius
        imageViewProfilePicture.setImageDrawable(rounded_bmp)

        btnAddRecipe.setOnClickListener{
            lateinit var recipe : HashMap<String, String>

            recipe = hashMapOf(
                "Title" to recipeTitle.toString(),
                "Description" to recipeDesc.toString(),
                "userEmailAddress" to userEmail.toString()
            )

            db.collection("myRecipes").document(recipeTitle)
                .set(recipe)
                .addOnSuccessListener {
                    Log.d(TAG, "Document has been added")
                    makeToast("New Recipe added")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error adding document: ", e)
                }
        }
    }

    fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()




}
