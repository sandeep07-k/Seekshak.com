package com.example.seekshakcom

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat


class StudentHomeActivity : AppCompatActivity() {

    private lateinit var homeBtn: ImageButton
    private lateinit var myPostBtn: ImageButton
    private lateinit var addPostBtn: ImageButton
    private lateinit var chatsBtn: ImageButton
    private lateinit var myAccountBtn: ImageButton
    private lateinit var myAccountText: TextView
    private lateinit var homeText: TextView
    private lateinit var myPostText: TextView
    private lateinit var chatsText: TextView

    private lateinit var fragmentContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val token = prefs.getString("token", null)
        Log.d("TokenDebug", "Token at StudentHomeActivity: $token")

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        homeBtn = findViewById(R.id.home_btn)
        homeText = findViewById(R.id.home_text)
        myPostBtn = findViewById(R.id.my_post)
        addPostBtn = findViewById(R.id.add_post)
        chatsBtn = findViewById(R.id.chats)
        myAccountBtn = findViewById(R.id.my_account)
        myAccountText = findViewById(R.id.my_account_text)
        myPostText = findViewById(R.id.myPostText)
        chatsText = findViewById(R.id.chatsText)
        fragmentContainer = findViewById(R.id.student_home_fragment_container)

        // Load default HomeFragment
        selectHomeTab()
        fragmentContainer.visibility = View.VISIBLE
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_home_fragment_container, HomeFragment())
            .commit()

        homeBtn.setOnClickListener {
            ifNotVisibleThenShow(HomeFragment()) {
                resetAllTabs()
                homeBtn.isSelected = true
                homeBtn.setImageResource(R.drawable.ic_home_filled)
                homeText.setTypeface(null, Typeface.BOLD)
                homeText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
            }
        }

        myPostBtn.setOnClickListener {
            ifNotVisibleThenShow(MyPostsFragment()) {
                resetAllTabs()
                myPostBtn.isSelected = true
                myPostBtn.setImageResource(R.drawable.ic_mypost_filled)
                myPostText.setTypeface(null, Typeface.BOLD)
                myPostText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
            }
        }

        chatsBtn.setOnClickListener {
            ifNotVisibleThenShow(ChatsFragment()) {
                resetAllTabs()
                chatsBtn.isSelected = true
                chatsBtn.setImageResource(R.drawable.ic_chats_filled)
                chatsText.setTypeface(null, Typeface.BOLD)
                chatsText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
            }
        }

        myAccountBtn.setOnClickListener {
            ifNotVisibleThenShow(MyAccountFragment()) {
                resetAllTabs()
                myAccountBtn.isSelected = true
                myAccountBtn.setImageResource(R.drawable.ic_account_filled)
                myAccountText.setTypeface(null, Typeface.BOLD)
                myAccountText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
            }
        }

        addPostBtn.setOnClickListener {
            startActivity(Intent(this, AddPostActivity::class.java))
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentFragment = supportFragmentManager.findFragmentById(R.id.student_home_fragment_container)
                if (currentFragment !is HomeFragment) {
                    resetAllTabs()
                    selectHomeTab()
                    supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.student_home_fragment_container, HomeFragment())
                        .commit()
                } else {
                    AlertDialog.Builder(this@StudentHomeActivity)
                        .setTitle("Exit App")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes") { _, _ -> finishAffinity() }
                        .setNegativeButton("No", null)
                        .show()
                }
            }
        })
    }

    private fun ifNotVisibleThenShow(fragment: androidx.fragment.app.Fragment, onSelected: () -> Unit) {
        val current = supportFragmentManager.findFragmentById(R.id.student_home_fragment_container)
        if (current?.javaClass != fragment.javaClass) {
            onSelected()
            supportFragmentManager.beginTransaction()
                .replace(R.id.student_home_fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }
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

    private fun selectHomeTab() {
        homeBtn.isSelected = true
        homeBtn.setImageResource(R.drawable.ic_home_filled)
        homeText.setTypeface(null, Typeface.BOLD)
        homeText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
    }
}
