package com.example.seekshakcom

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.model.MyPost
import java.text.SimpleDateFormat
import java.util.*

class MyPostAdapter(
    private val postList: List<MyPost>,
    private val onRepost: (MyPost) -> Unit,
    private val onMarkFilled: (MyPost) -> Unit,
    private val onRemove: (MyPost) -> Unit,
    private val onEdit: (MyPost) -> Unit
) : RecyclerView.Adapter<MyPostAdapter.PostViewHolder>() {

    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postedDate: TextView = itemView.findViewById(R.id.from_to_date)
        val tuitionCode: TextView = itemView.findViewById(R.id.tuition_Code)
        val classDetails: TextView = itemView.findViewById(R.id.class_Details)
        val subjectDetails: TextView = itemView.findViewById(R.id.subject_Details)
        val boardDetails: TextView = itemView.findViewById(R.id.board_Details)
        val genderPreference: TextView = itemView.findViewById(R.id.gender_Preference)
        val expectedFee: TextView = itemView.findViewById(R.id.fee_Details)
        val classTiming: TextView = itemView.findViewById(R.id.class_Timing)
        val classSchedule: TextView = itemView.findViewById(R.id.class_Schedule)
        val modeOfClass: TextView = itemView.findViewById(R.id.mode_Of_Class)
        val qualification: TextView = itemView.findViewById(R.id.min_Qualification)
        val demoClassDate: TextView = itemView.findViewById(R.id.demo_Class_Date)
        val specialReq: TextView = itemView.findViewById(R.id.special_Requirement)
        val locationDetails: TextView = itemView.findViewById(R.id.location_details)

        val btnTotalApplications: Button = itemView.findViewById(R.id.btn_total_applications)
        val btnEditDetails: Button = itemView.findViewById(R.id.btn_Edit_Details)
        val btnRepost: Button = itemView.findViewById(R.id.btn_repost)
        val btnMarkFilled: Button = itemView.findViewById(R.id.btn_mark_filled)

        val layoutExpired: LinearLayout = itemView.findViewById(R.id.layout_expired_actions)
        val moreOptions: ImageButton = itemView.findViewById(R.id.moreOptionsButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]

        // Format createdAt date
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        try {
            val date = inputFormat.parse(post.createdAt)
            val calendar = Calendar.getInstance()
            calendar.time = date!!
            val fromDate = outputFormat.format(calendar.time)
            calendar.add(Calendar.MONTH, 1)
            val toDate = outputFormat.format(calendar.time)
            holder.postedDate.text = "FROM: $fromDate - TO: $toDate"
        } catch (e: Exception) {
            holder.postedDate.text = "Invalid Date"
        }

        // Set field values
        holder.tuitionCode.text = "Tuition Code: ${post.tuitionCode}"
        holder.classDetails.text = "Class: ${post.className}"
        holder.subjectDetails.text = "Subject: ${post.subject}"
        holder.boardDetails.text = "Education Board: ${post.educationBoard}"
        holder.genderPreference.text = "Gender Preference: ${post.gender}"
        holder.expectedFee.text = "Expected Fee: ${post.fee} "
        holder.classTiming.text = "Class Timing: ${post.classTiming}"
        holder.classSchedule.text = "Class Schedule: ${post.classSchedule}"
        holder.modeOfClass.text = "Mode: ${post.modeOfClass}"
        holder.qualification.text = "Min Qualification: ${post.qualification}"
        holder.demoClassDate.text = "Demo Class Date: ${if (post.demoClassDate.isBlank()) "None" else post.demoClassDate}"
        holder.specialReq.text = "Special Req: ${if (post.specialReq.isBlank()) "None" else post.specialReq}"

        // Address: sublocality, area, city
        val locationDisplay = listOfNotNull(
            post.sublocality?.takeIf { it.isNotBlank() },
            post.area?.takeIf { it.isNotBlank() },
            post.city?.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "Unknown" }
        holder.locationDetails.text = "Address: $locationDisplay"

        // Status logic
        if (post.status == "expired") {
            holder.itemView.alpha = 0.5f
            holder.layoutExpired.visibility = View.VISIBLE
        } else {
            holder.itemView.alpha = 1f
            holder.layoutExpired.visibility = View.GONE
        }

        // Button actions
        holder.btnEditDetails.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, EditPostActivity::class.java)
            intent.putExtra("POST_DATA", postList[position])
            context.startActivity(intent)
        }

        holder.btnTotalApplications.setOnClickListener {
            // TODO
        }

        holder.btnRepost.setOnClickListener {
            onRepost(post)
        }

        holder.btnMarkFilled.setOnClickListener {
            onMarkFilled(post)
        }

        holder.moreOptions.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_remove -> {
                        onRemove(post)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = postList.size
}
