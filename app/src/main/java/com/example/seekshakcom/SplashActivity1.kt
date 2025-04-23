package com.example.seekshakcom

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity1 : AppCompatActivity() {

    private lateinit var video1: VideoView
    private lateinit var video2: VideoView

    private var isVideo1Prepared = false
    private var isVideo2Prepared = false
    private var longestDuration = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash1)

        video1 = findViewById(R.id.video1)
        video2 = findViewById(R.id.video2)

        val uri1 = Uri.parse("android.resource://$packageName/${R.raw.video1}")
        val uri2 = Uri.parse("android.resource://$packageName/${R.raw.video2}")

        video1.setVideoURI(uri1)
        video2.setVideoURI(uri2)

        video1.setOnPreparedListener { mediaPlayer ->
            isVideo1Prepared = true
            mediaPlayer.isLooping = true
            longestDuration = maxOf(longestDuration, mediaPlayer.duration)
            startIfReady()
        }

        video2.setOnPreparedListener { mediaPlayer ->
            isVideo2Prepared = true
            mediaPlayer.isLooping = false
            longestDuration = maxOf(longestDuration, mediaPlayer.duration)
            startIfReady()
        }

        video1.setOnCompletionListener {
            checkAndProceed()
        }

        video2.setOnCompletionListener {
            checkAndProceed()
        }
    }

    private fun startIfReady() {
        if (isVideo1Prepared && isVideo2Prepared) {
            video1.start()
            video2.start()
        }
    }

    private fun checkAndProceed() {
        // Check if both videos finished
        if ( !video2.isPlaying) {
            startActivity(Intent(this, SplashActivity2::class.java))
            finish()
        }
    }
}
