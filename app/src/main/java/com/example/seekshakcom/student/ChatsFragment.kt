package com.example.seekshakcom.student

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.ChatItem
import com.google.android.material.tabs.TabLayout

class ChatsFragment : Fragment(R.layout.fragment_chats) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var chatList: List<ChatItem>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarChats)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Chats"


        recyclerView = view.findViewById(R.id.recyclerViewChats)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        chatList = List(10) {
            ChatItem(
                username = "User $it",
                lastMessage = "Last message from user $it...",
                time = "12:${10 + it} PM"
            )
        }

        chatAdapter = ChatAdapter(chatList)
        recyclerView.adapter = chatAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutChats)
        tabLayout.addTab(tabLayout.newTab().setText("All Chats"))
        tabLayout.addTab(tabLayout.newTab().setText("Unread"))
    }

}
