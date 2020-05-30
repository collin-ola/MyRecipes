package com.vcapps.myrecipesapp

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

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
    lateinit var ingredientList :MutableList<Ingredient>
    private val db = Firebase.firestore

    fun addIngredient(ingredientToAdd :Ingredient){
       ingredientList.add(ingredientToAdd)
    }

    fun uploadRecipe(emailAddress :String) {
        val recipe = hashMapOf(
            "Title" to recipeName,
            "Description" to recipeCaption,
            "Category" to recipeCategory
            //"Ingredients" to "
        )



        /*db.collection("myRecipes").document(emailAddress)
            .set(recipe)
            .addOnSuccessListener {
                Log.d(TAG, "Document has been added")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document: ", e)
            }*/

    }

}