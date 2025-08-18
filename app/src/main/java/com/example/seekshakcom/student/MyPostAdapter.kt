package com.example.seekshakcom.student

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.MyPost
import java.text.SimpleDateFormat
import java.util.*
import com.example.seekshakcom.utils.TextUtilsHelper.setBoldLabel

class MyPostAdapter(
    private val postList: List<MyPost>,
    private val onRepost: (MyPost) -> Unit,
    private val onRemove: (MyPost) -> Unit,
    private val onMarkFilled: (MyPost) -> Unit,
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
        val btnRemove: Button = itemView.findViewById(R.id.btn_remove)

        val viewPost: View = itemView.findViewById(R.id.whiteBackgroundBottom)
        val layoutPost: LinearLayout = itemView.findViewById(R.id.layout_post)
        val layoutActive: LinearLayout = itemView.findViewById(R.id.layout_active_actions)
        val layoutExpired: LinearLayout = itemView.findViewById(R.id.layout_expired_actions)
        val layoutFilled: LinearLayout = itemView.findViewById(R.id.layout_filled_actions)
        val moreOptions: ImageButton = itemView.findViewById(R.id.moreOptionsButton)
        val filledText: TextView = itemView.findViewById(R.id.filledText)
        val expiredText: TextView = itemView.findViewById(R.id.expiredText)
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
        holder.tuitionCode.text = setBoldLabel("Tuition Code: ", post.tuitionCode.toString())
        holder.classDetails.text = setBoldLabel("Class: ", post.className)
        holder.subjectDetails.text = setBoldLabel("Subject: ", post.subject)
        holder.boardDetails.text = setBoldLabel("Education Board: ", post.educationBoard)
        holder.genderPreference.text = setBoldLabel("Gender Preference: ", post.gender)
        holder.expectedFee.text = setBoldLabel("Expected Fee: ", post.fee)
        holder.classTiming.text = setBoldLabel("Class Timing: ", post.classTiming)
        holder.classSchedule.text = setBoldLabel("Class Schedule: ", post.classSchedule)
        holder.modeOfClass.text = setBoldLabel("Mode: ", post.modeOfClass)
        holder.qualification.text = setBoldLabel("Min Qualification: ", post.qualification)
        holder.demoClassDate.text = setBoldLabel(
            "Demo Class Date: ",
            if (post.demoClassDate.isBlank()) "None" else post.demoClassDate
        )
        holder.specialReq.text = setBoldLabel(
            "Special Req: ",
            if (post.specialReq.isBlank()) "None" else post.specialReq
        )


        // Address: sublocality, area, city with bold label
        val locationDisplay = listOfNotNull(
            post.sublocality?.takeIf { it.isNotBlank() },
            post.area?.takeIf { it.isNotBlank() },
            post.city?.takeIf { it.isNotBlank() }
        ).joinToString(", ").ifBlank { "Unknown" }
        holder.locationDetails.text = setBoldLabel("Address: ", locationDisplay)

        // Status logic
        when (post.status) {
            "expired" -> {
                holder.layoutPost.alpha = 0.5f
                holder.viewPost.visibility = View.VISIBLE
                holder.layoutExpired.visibility = View.VISIBLE
                holder.layoutActive.visibility = View.GONE
                holder.layoutFilled.visibility = View.GONE
                holder.moreOptions.isEnabled = false
                holder.expiredText.visibility = View.VISIBLE
                holder.filledText.visibility = View.GONE
            }
            "filled" -> {
                holder.layoutPost.alpha = 0.5f
                holder.viewPost.visibility = View.VISIBLE
                holder.filledText.visibility = View.VISIBLE
                holder.layoutFilled.visibility = View.VISIBLE
                holder.layoutExpired.visibility = View.GONE
                holder.layoutActive.visibility = View.GONE
                holder.moreOptions.isEnabled = false
                holder.expiredText.visibility = View.GONE
            }
            "active" -> {
                holder.layoutPost.alpha = 1f
                holder.layoutActive.visibility = View.VISIBLE
                holder.viewPost.visibility = View.GONE
                holder.layoutExpired.visibility = View.GONE
                holder.layoutFilled.visibility = View.GONE
                holder.moreOptions.isEnabled = true
                holder.expiredText.visibility = View.GONE
                holder.filledText.visibility = View.GONE

            }
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

        holder.btnRemove.setOnClickListener {
            onRemove(post)
        }

        holder.moreOptions.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_post_options, popup.menu)
            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.menu_remove -> {
                        onRemove(post) // existing logic
                        true
                    }
                    R.id.menu_mark_filled -> {
                        onMarkFilled(post) // new callback
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
