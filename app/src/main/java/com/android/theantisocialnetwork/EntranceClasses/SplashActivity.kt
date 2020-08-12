package com.android.theantisocialnetwork.EntranceClasses

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.theantisocialnetwork.MainActivity
import com.android.theantisocialnetwork.R

class SplashActivity : AppCompatActivity() {

    private lateinit var tv: TextView
    private lateinit var iv: ImageView
    // This is the loading time of the splash screen
    private val splashTimeOut:Long = 1350 // 1 sec

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splashscreen)


        // Unique animations for the logo and name
        tv = findViewById(R.id.textViewCompanyName)
        iv = findViewById(R.id.imageViewLogoUi)
        val splashFadeIn = AnimationUtils.loadAnimation(this,
            R.anim.fade_in)
        val splashFadeOut = AnimationUtils.loadAnimation(this,
            R.anim.fade_out)
        val logoFadeOut= AnimationUtils.loadAnimation(this,
            R.anim.logo_fade_out)

        tv.startAnimation(splashFadeIn)
        tv.startAnimation(splashFadeOut)

        // This method will execute once the timer is over
        Handler().postDelayed({
            iv.startAnimation(logoFadeOut)

            // Start main activity
            startActivity(Intent(this,
                MainActivity::class.java))
            overridePendingTransition(
                android.R.anim.fade_in,
                R.anim.fade_out
            )
            // Close this activity
            finish()
        }, splashTimeOut)
    }
}