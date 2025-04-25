package com.example.seekshakcom

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class SplashActivity2 : AppCompatActivity() {

    private lateinit var video3: VideoView
    private lateinit var video4: VideoView

    private var isVideo1Prepared = false
    private var isVideo2Prepared = false
    private var longestDuration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash2)

        // 1. Allow layout to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 2. Make status bar transparent
        Color.TRANSPARENT.also { window.statusBarColor = it }
        // 3. Optional: Change status bar icon color (dark icons = true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        video3 = findViewById(R.id.video3)
        video4 = findViewById(R.id.video4)

        val uri1 = Uri.parse("android.resource://$packageName/${R.raw.video3}")
        val uri2 = Uri.parse("android.resource://$packageName/${R.raw.video4}")

        video3.setVideoURI(uri1)
        video4.setVideoURI(uri2)

        video3.setOnPreparedListener { mediaPlayer ->
            isVideo1Prepared = true
            mediaPlayer.isLooping = true
            longestDuration = maxOf(longestDuration, mediaPlayer.duration)
            startIfReady()
        }

        video4.setOnPreparedListener { mediaPlayer ->
            isVideo2Prepared = true
            mediaPlayer.isLooping = false
            longestDuration = maxOf(longestDuration, mediaPlayer.duration)
            startIfReady()
        }

        video3.setOnCompletionListener {
            checkAndProceed()
        }

        video4.setOnCompletionListener {
            checkAndProceed()
        }
    }

    private fun startIfReady() {
        if (isVideo1Prepared && isVideo2Prepared) {
            video3.start()
            video4.start()
        }
    }

    private fun checkAndProceed() {
        // Check if both videos finished
        if ( !video4.isPlaying) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
