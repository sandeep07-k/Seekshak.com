@file:Suppress("DEPRECATION")

package com.example.seekshakcom


import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.ui.myaccount.MyAccountFragment

class StudentHomeActivity : AppCompatActivity() {


    private lateinit var homeBtn: ImageButton
    private lateinit var myPostBtn: ImageButton
    private lateinit var addPostBtn: ImageButton
    private lateinit var chatsBtn: ImageButton
    private lateinit var myAccountBtn: ImageButton
    private lateinit var myAccountText: TextView
    private lateinit var homeText: TextView
    private lateinit var myPostText:TextView
    private lateinit var chatsText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        // 1. Allow layout to draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 2. Make status bar transparent
        Color.TRANSPARENT.also { window.statusBarColor = it }

        // 3. Optional: Change status bar icon color (dark icons = true)
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true



        // Bottom Navigation
        homeBtn = findViewById(R.id.home_btn)
        homeText = findViewById(R.id.home_text)
        myPostBtn = findViewById(R.id.my_post)
        addPostBtn = findViewById(R.id.add_post)
        chatsBtn = findViewById(R.id.chats)
        myAccountBtn = findViewById(R.id.my_account)
        myAccountText = findViewById(R.id.my_account_text)
        myPostText = findViewById(R.id.myPostText)
        chatsText = findViewById(R.id.chatsText)

        // Set HomeFragment as default screen on launch
        val fragment = HomeFragment()
        val fragmentContainer = findViewById<FrameLayout>(R.id.student_home_fragment_container)
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_home_fragment_container, fragment)
            .commit()





        // Listeners

        homeBtn.setOnClickListener {
            resetAllTabs()
            homeBtn.isSelected = true
            homeBtn.setImageResource(R.drawable.ic_home_filled) // use filled icon
            homeText.setTypeface(null, Typeface.BOLD)
            homeText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.darkest_blue
                )
            ) // or your highlight color

            fragmentContainer.visibility = View.VISIBLE // Show fragment
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,  // enter
                    android.R.anim.slide_out_right, // exit
                    android.R.anim.slide_in_left,  // popEnter
                    android.R.anim.slide_out_right // popExit
                )
                .replace(R.id.student_home_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        myPostBtn.setOnClickListener {
            resetAllTabs()
            myPostBtn.isSelected = true
            myPostBtn.setImageResource(R.drawable.ic_mypost_filled) // use filled icon
            myPostText.setTypeface(null, Typeface.BOLD)
            myPostText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.darkest_blue
                )
            )
            // startActivity(Intent(this, MyPostsActivity::class.java))
        }

        addPostBtn.setOnClickListener {
            val intent = Intent(this, AddPostActivity::class.java)
            startActivity(intent)
        }


        chatsBtn.setOnClickListener {
            resetAllTabs()
            chatsBtn.isSelected = true
            chatsBtn.setImageResource(R.drawable.ic_chats_filled) // use filled icon
            chatsText.setTypeface(null, Typeface.BOLD)
            chatsText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.darkest_blue
                )
            )
            val fragment = ChatsFragment()
            fragmentContainer.visibility = View.VISIBLE
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,  // enter
                    android.R.anim.slide_out_right, // exit
                    android.R.anim.slide_in_left,  // popEnter
                    android.R.anim.slide_out_right // popExit
                )
                .replace(R.id.student_home_fragment_container, fragment)
                .addToBackStack(null)
                .commit()

        }

        myAccountBtn.setOnClickListener {
            resetAllTabs()
            myAccountBtn.isSelected = true
            myAccountBtn.setImageResource(R.drawable.ic_account_filled) // use filled icon
            myAccountText.setTypeface(null, Typeface.BOLD)
            myAccountText.setTextColor(
                ContextCompat.getColor(
                    this,
                    R.color.darkest_blue
                )
            ) // or your highlight color

            val fragment = MyAccountFragment()
            fragmentContainer.visibility = View.VISIBLE // Show fragment

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.slide_in_left,  // enter
                    android.R.anim.slide_out_right, // exit
                    android.R.anim.slide_in_left,  // popEnter
                    android.R.anim.slide_out_right // popExit
                )
                .replace(R.id.student_home_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fm = supportFragmentManager
                if (fm.backStackEntryCount > 0) {
                    fm.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })


    }

    private fun resetTab(icon: ImageButton, label: TextView, defaultIconRes: Int) {
        icon.setImageResource(defaultIconRes)
        icon.isSelected = false
        label.setTypeface(null, Typeface.NORMAL)
        label.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
    }

    private fun resetAllTabs() {
        resetTab(myAccountBtn, myAccountText, R.drawable.ic_account)
        resetTab(homeBtn, homeText, R.drawable.ic_home)
        resetTab(chatsBtn, chatsText, R.drawable.ic_chats)
        resetTab(myPostBtn, myPostText, R.drawable.ic_mypost)


    }







}
