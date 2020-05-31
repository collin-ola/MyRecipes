package com.vcapps.myrecipesapp

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_registration.*

class ProfileActivity : AppCompatActivity() {

    //private lateinit var recipeList: ListView

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
       
        val userEmail: String? = intent.getStringExtra("userEmailAddress")

        //Create rounded profile picture//
        val bmp = BitmapFactory.decodeResource(resources, R.drawable.profile)
        val rounded_bmp = RoundedBitmapDrawableFactory.create(resources, bmp)
        rounded_bmp.cornerRadius
        imageViewProfilePicture.setImageDrawable(rounded_bmp)

        btnAddRecipe.setOnClickListener{
            createTestRecipe(userEmail!!)
        }
    }

    private fun createTestRecipe(userEmail :String){
        val recipeTitle = "Victoria's Amazing Banananana Pancakes"
        val recipeDesc = "This is a Plantain pancakes recipe I learnt from my ancestors and I really love it. I hope you like it too"
        val testRecipe = Recipe(recipeTitle, recipeDesc, RecipeCategory.breakfast)
        val testIngredient = Ingredient("Plantain", "2", "Pieces")
        val testIngredient2 = Ingredient("Flour", "100", "grams")
        testRecipe.addIngredient(testIngredient)
        testRecipe.addIngredient(testIngredient2)

        testRecipe.uploadRecipe(userEmail)
        testRecipe.downloadRecipes(userEmail)
    }

    private fun makeToast(toastText :String) = Toast.makeText(applicationContext, toastText, Toast.LENGTH_LONG).show()
}
