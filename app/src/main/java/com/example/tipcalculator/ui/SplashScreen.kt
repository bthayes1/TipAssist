package com.example.tipcalculator.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.tipcalculator.R

@SuppressLint("CustomSplashScreen")
class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        supportActionBar?.hide() // Remove action bar

        //Initialize views
        val splashConstraint = findViewById<ConstraintLayout>(R.id.splashConstraint)
        val backgroundImage: ImageView = findViewById(R.id.SplashScreenImage)

        // This makes TipAssist logo slide in
        val slideAnimation = AnimationUtils.loadAnimation(this, R.anim.side_slide)

        // Setup the background animation
        backgroundImage.startAnimation(slideAnimation)
        val gradientAnim = splashConstraint.background as AnimationDrawable
        gradientAnim.setEnterFadeDuration(100)
        gradientAnim.setExitFadeDuration(1000)
        gradientAnim.start()

        // Move to next page after time delay
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 4000)
    }
}
