package com.example.seekshakcom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.model.MyPost

class MyPostsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var myPostAdapter: MyPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_my_posts, container, false)


        // Setup RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewPosts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        myPostAdapter = MyPostAdapter(getDummyData()) // Replace with your real data
        recyclerView.adapter = myPostAdapter

        return view
    }

    private fun getDummyData(): List<MyPost> {
        return listOf(
            MyPost("01-04-2025", "T-10001", "Class 10", "Math, Science", "CBSE", "2", "Male", "5000"),
            MyPost("02-04-2025", "T-10002", "Class 9", "English, History", "ICSE", "1.5", "Female", "4500")
        )
    }
}
