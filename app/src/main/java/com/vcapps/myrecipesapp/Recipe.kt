package com.vcapps.myrecipesapp

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

object RecipeCategory {
    const val breakfast = "Breakfast"
    const val lunch = "Lunch"
    const val dinner = "Dinner"
    const val snack = "Snack"
    const val dessert = "Dessert"
}

class Recipe(_recipeName:String, _recipeCaption:String, _recipeCategory: String) {
    private var recipeName = _recipeName
    private var recipeCaption = _recipeCaption
    private var recipeCategory = _recipeCategory
    var ingredientList = mutableListOf<Ingredient>()
    private val db = Firebase.firestore

    fun addIngredient(ingredientToAdd :Ingredient){
       ingredientList.add(ingredientToAdd)
    }

    fun uploadRecipe(emailAddress :String) {
         val recipe = hashMapOf(
            "Title" to recipeName,
            "Description" to recipeCaption,
            "Category" to recipeCategory,
            "Ingredients" to Gson().toJson(ingredientList)
        )

        db.collection("myRecipes_$emailAddress").document(recipeName)
            .set(recipe)
            .addOnSuccessListener {
                Log.d(TAG, "Document has been added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
            }
    }

    fun downloadRecipes(emailAddress: String) {
        db.collection("myRecipes_$emailAddress") //need to iterate through each document
            .get()
            .addOnSuccessListener {documents ->
                documents.forEach {recipe ->
                    Log.d(TAG, "Recipe: ${recipe.data}")
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
            }
    }
}