package com.vcapps.myrecipesapp

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val bmp = BitmapFactory.decodeResource(resources, R.drawable.profile)
        val rounded_bmp = RoundedBitmapDrawableFactory.create(resources, bmp)
        rounded_bmp.cornerRadius
        imageViewProfilePicture.setImageDrawable(rounded_bmp)
    }


}
