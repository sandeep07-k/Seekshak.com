package com.example.seekshakcom.student

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.seekshakcom.R
import com.example.seekshakcom.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullImageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageUrl = intent.getStringExtra("imageUrl")

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_user)
            .error(R.drawable.ic_user)
            .into(binding.fullImageView)

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}
