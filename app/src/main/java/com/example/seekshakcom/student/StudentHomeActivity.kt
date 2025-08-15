package com.example.seekshakcom.student

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.seekshakcom.R


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
    private lateinit var homeLayout: LinearLayout
    private lateinit var myAccountLayout: LinearLayout
    private lateinit var chatBtnLayout: LinearLayout
    private lateinit var myPostbtnlayout: LinearLayout


    private lateinit var fragmentContainer: FrameLayout

    // Cached fragment instances
    private val homeFragment = HomeFragment()
    private val myPostsFragment = MyPostsFragment()
    private val chatsFragment = ChatsFragment()
    private val myAccountFragment = MyAccountFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

//        val prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE)
//        val token = prefs.getString("token", null)


        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        homeBtn = findViewById(R.id.home_btn)
        homeText = findViewById(R.id.home_text)
        homeLayout = findViewById(R.id.home_btn_layout)
        myPostBtn = findViewById(R.id.my_post)
        myPostbtnlayout = findViewById(R.id.myPostbtn_layout)
        addPostBtn = findViewById(R.id.add_post)
        chatsBtn = findViewById(R.id.chats)
        myAccountBtn = findViewById(R.id.my_account)
        myAccountText = findViewById(R.id.my_account_text)
        myAccountLayout = findViewById(R.id.my_account_layout)
        myPostText = findViewById(R.id.myPostText)
        chatsText = findViewById(R.id.chatsText)
        chatBtnLayout = findViewById(R.id.chatbtn_layout)
        fragmentContainer = findViewById(R.id.student_home_fragment_container)

        fragmentContainer.visibility = View.VISIBLE
        selectHomeTab()
        showFragment(homeFragment)

        homeBtn.setOnClickListener {
            handleHomeClick()
        }

        homeLayout.setOnClickListener {
            handleHomeClick()
        }

        myPostBtn.setOnClickListener {
            handlePostClick()
        }
        myPostbtnlayout.setOnClickListener{
            handlePostClick()
        }

        chatsBtn.setOnClickListener {
           handleChatsClick()
        }
        chatBtnLayout.setOnClickListener{
            handleChatsClick()
        }

        myAccountBtn.setOnClickListener {
            handleAccountClick()
        }
        myAccountLayout.setOnClickListener{
            handleAccountClick()
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
                    showFragment(homeFragment)
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

    private fun handleHomeClick() {
        ifNotVisibleThenShow(homeFragment) {
            resetAllTabs()
            window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
            homeBtn.setImageResource(R.drawable.ic_home_filled)
            homeBtn.isSelected = true
            homeText.setTypeface(null, Typeface.BOLD)
            homeText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
        }
    }
    private fun handleAccountClick(){
        ifNotVisibleThenShow(myAccountFragment) {
            resetAllTabs()
            window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
            myAccountBtn.setImageResource(R.drawable.ic_account_filled)
            myAccountBtn.isSelected = true
            myAccountText.setTypeface(null, Typeface.BOLD)
            myAccountText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
        }
    }
    private fun handleChatsClick(){
        ifNotVisibleThenShow(chatsFragment) {
            resetAllTabs()
            window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
            chatsBtn.setImageResource(R.drawable.ic_chats_filled)
            chatsBtn.isSelected = true
            chatsText.setTypeface(null, Typeface.BOLD)
            chatsText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
        }
    }
    private fun handlePostClick(){
        ifNotVisibleThenShow(myPostsFragment) {
            resetAllTabs()
            window.statusBarColor = ContextCompat.getColor(this, R.color.soft_blue)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
            myPostBtn.setImageResource(R.drawable.ic_mypost_filled)
            myPostBtn.isSelected = true
            myPostText.setTypeface(null, Typeface.BOLD)
            myPostText.setTextColor(ContextCompat.getColor(this, R.color.darkest_blue))
        }
    }

    private fun ifNotVisibleThenShow(fragment: androidx.fragment.app.Fragment, onSelected: () -> Unit) {
        val current = supportFragmentManager.findFragmentById(R.id.student_home_fragment_container)
        if (current?.javaClass != fragment.javaClass) {
            onSelected()
            showFragment(fragment)
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.student_home_fragment_container, fragment)
            .commit()
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
