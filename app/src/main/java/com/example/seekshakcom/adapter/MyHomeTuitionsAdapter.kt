package com.example.seekshakcom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.TuitionPost
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale

class MyHomeTuitionsAdapter(
    private val items: List<TuitionPost>,
    private val onApplyClick: (TuitionPost) -> Unit,
    private val onFavouriteClick: (TuitionPost) -> Unit
) : RecyclerView.Adapter<MyHomeTuitionsAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val postingDate: TextView = itemView.findViewById(R.id.posting_date)
        val tuitionStatus: TextView = itemView.findViewById(R.id.tuition_status)
        val tuitionCode: TextView = itemView.findViewById(R.id.tuition_Code)
        val classDetails: TextView = itemView.findViewById(R.id.class_Details)
        val subjectDetails: TextView = itemView.findViewById(R.id.subject_Details)
        val boardDetails: TextView = itemView.findViewById(R.id.board_Details)
        val feeDetails: TextView = itemView.findViewById(R.id.fee_Details)
        val classTiming: TextView = itemView.findViewById(R.id.class_Timing)
        val classSchedule: TextView = itemView.findViewById(R.id.class_Schedule)
        val locationDetails: TextView = itemView.findViewById(R.id.location_details)
        val genderPreference: TextView = itemView.findViewById(R.id.gender_Preference)
        val minQualification: TextView = itemView.findViewById(R.id.min_Qualification)
        val modeOfClass: TextView = itemView.findViewById(R.id.mode_Of_Class)
        val demoClassDate: TextView = itemView.findViewById(R.id.demo_Class_Date)
        val specialRequirement: TextView = itemView.findViewById(R.id.special_Requirement)
        val distanceDetails: TextView = itemView.findViewById(R.id.distance_Details)

        val applyBtn: Button = itemView.findViewById(R.id.applyBtn)
        val favouriteBtn: ImageButton = itemView.findViewById(R.id.favourite_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tuition_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post = items[position]

        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MMM-yy", Locale.getDefault())

        val date = inputFormat.parse(post.postedDate) // parse the original string
        val formattedDate = outputFormat.format(date) // format to desired string

        holder.postingDate.text = "Posting date: $formattedDate"
        holder.tuitionStatus.text = "Status: ${post.status}"
        holder.tuitionCode.text = "Tuition Code: ${post.tuitionCode}"
        holder.classDetails.text = "Class: ${post.className}"
        holder.subjectDetails.text = "Subject: ${post.subject}"
        holder.boardDetails.text = "Education Board: ${post.educationBoard}"
        holder.feeDetails.text = "Expected Fee: ${post.fee ?: "N/A"}"
        holder.classTiming.text = "Time: ${post.classTiming}"
        holder.classSchedule.text = "Schedule: ${post.classSchedule}"
        holder.locationDetails.text =
            "Address: ${post.sublocality}, ${post.area}, ${post.city}"
        holder.genderPreference.text = "Gender Preference: ${post.gender}"
        holder.minQualification.text = "Min. Qualification: ${post.qualification}"
        holder.modeOfClass.text = "Mode: ${post.modeOfClass}"
        holder.demoClassDate.text = "Demo Class Date: ${post.demoClassDate ?: "N/A"}"
        holder.specialRequirement.text = "Special Req: ${post.specialReq ?: "None"}"

        holder.distanceDetails.text = post.distanceInKm?.let { km ->
            if (km < 1) {
                val meters = (km * 1000).roundToInt()  // round to nearest integer
                "Distance: $meters m"
            } else {
                "Distance: ${"%.2f".format(km)} km"
            }
        } ?: run {
            post.distanceInMeters?.let { meters ->
                "Distance: ${meters.roundToInt()} m"
            } ?: "Distance: N/A"
        }


        // Handle button clicks
        holder.applyBtn.setOnClickListener { onApplyClick(post) }
        holder.favouriteBtn.setOnClickListener { onFavouriteClick(post) }
    }

    override fun getItemCount() = items.size
}
