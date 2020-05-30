package com.vcapps.myrecipesapp

data class Ingredient(var ingredientName :String, var ingredientQuantity :String, var ingredientUnit :String){
    override fun toString(): String {
        return "Name: $ingredientName; Quantity: $ingredientQuantity; Unit: $ingredientUnit"
    }
}