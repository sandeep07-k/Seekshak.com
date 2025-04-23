package com.example.seekshakcom

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var menuBar: ImageButton
    private lateinit var searchEditText: EditText
    private lateinit var notificationButton: ImageButton
    private lateinit var creditBalance: TextView
    private lateinit var creditCoin: ImageView

    private lateinit var homeBtn: ImageButton
    private lateinit var myPostBtn: ImageButton
    private lateinit var addPostBtn: ImageButton
    private lateinit var chatsBtn: ImageButton
    private lateinit var myAccountBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_home)

        // Top Section
        menuBar = findViewById(R.id.menu_bar)
        searchEditText = findViewById(R.id.searchEditText)
        notificationButton = findViewById(R.id.notification_Btn)
        creditBalance = findViewById(R.id.credit_balance)
        creditCoin = findViewById(R.id.credit_coin)

        // Bottom Navigation
        homeBtn = findViewById(R.id.home_btn)
        myPostBtn = findViewById(R.id.my_post)
        addPostBtn = findViewById(R.id.add_post)
        chatsBtn = findViewById(R.id.chats)
        myAccountBtn = findViewById(R.id.my_account)

        // Listeners
        menuBar.setOnClickListener {
            Toast.makeText(this, "Menu clicked", Toast.LENGTH_SHORT).show()
        }


        notificationButton.setOnClickListener {
            Toast.makeText(this, "Notifications", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, NotificationActivity::class.java))
        }

        creditCoin.setOnClickListener {
            Toast.makeText(this, "Credits: ${creditBalance.text}", Toast.LENGTH_SHORT).show()
        }

        homeBtn.setOnClickListener {
            Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show()
        }

        myPostBtn.setOnClickListener {
            Toast.makeText(this, "My Posts", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, MyPostsActivity::class.java))
        }

        addPostBtn.setOnClickListener {
            Toast.makeText(this, "Add Post", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, AddPostActivity::class.java))
        }

        chatsBtn.setOnClickListener {
            Toast.makeText(this, "Chats", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, ChatActivity::class.java))
        }

        myAccountBtn.setOnClickListener {
            Toast.makeText(this, "My Account", Toast.LENGTH_SHORT).show()
            // startActivity(Intent(this, AccountActivity::class.java))
        }
    }
}
