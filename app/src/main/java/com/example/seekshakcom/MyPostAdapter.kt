package com.example.seekshakcom

import com.example.seekshakcom.model.MyPost

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MyPostAdapter(private val postList: List<MyPost>) :
    RecyclerView.Adapter<MyPostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postedDate: TextView = itemView.findViewById(R.id.from_to_date)
        val tuitionCode: TextView = itemView.findViewById(R.id.tuition_Code)
        val classDetails: TextView = itemView.findViewById(R.id.class_Details)
        val subjectDetails: TextView = itemView.findViewById(R.id.subject_Details)
        val boardDetails: TextView = itemView.findViewById(R.id.board_Details)
        val duration: TextView = itemView.findViewById(R.id.duration_Details)
        val genderPreference: TextView = itemView.findViewById(R.id.gender_Preference)
        val expectedFee: TextView = itemView.findViewById(R.id.fee_Details)
        val btnTotalApplications: Button = itemView.findViewById(R.id.btn_total_applications)
        val btnViewDetails: Button = itemView.findViewById(R.id.btn_Edit_Details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Update Posted Date to FROM and TO
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        try {
            val postDate = inputFormat.parse(post.postedDate)
            val calendar = Calendar.getInstance()
            calendar.time = postDate!!

            val fromDate = outputFormat.format(calendar.time)

            calendar.add(Calendar.MONTH, 1)
            val toDate = outputFormat.format(calendar.time)

            holder.postedDate.text = "FROM: $fromDate - TO: $toDate"

        } catch (e: Exception) {
            e.printStackTrace()
            holder.postedDate.text = "Invalid Date"
        }

        holder.tuitionCode.text = "Tuition Code: ${post.tuitionCode}"
        holder.classDetails.text = "Class: ${post.classDetails}"
        holder.subjectDetails.text = "Subject: ${post.subjectDetails}"
        holder.boardDetails.text = "Education Board: ${post.boardDetails}"
        holder.duration.text = "Duration: ${post.duration} hrs/day"
        holder.genderPreference.text = "Gender Preference: ${post.genderPreference}"
        holder.expectedFee.text = "Expected Fee: ${post.expectedFee} rs/month"

        holder.btnTotalApplications.setOnClickListener {
            // TODO: handle total applications click
        }
        holder.btnViewDetails.setOnClickListener {
            // TODO: handle view details click
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
}
