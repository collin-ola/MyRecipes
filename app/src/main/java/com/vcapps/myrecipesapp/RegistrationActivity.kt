package com.vcapps.myrecipesapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(R.layout.activity_registration)
        submitButton.setOnClickListener {
            Toast.makeText(applicationContext, "Clicked Submit", Toast.LENGTH_LONG).show()
        }
    }
}