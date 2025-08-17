package com.example.seekshakcom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.TuitionPost
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.seekshakcom.utils.TextUtilsHelper.setBoldLabel
import android.graphics.Color

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

        val layoutTuitionCard: LinearLayout = itemView.findViewById(R.id.layout_tution_card)
        val expiredText: TextView = itemView.findViewById(R.id.expiredText)
        val filledText: TextView = itemView.findViewById(R.id.filledText)
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

        holder.postingDate.text = setBoldLabel("Posting date: ", formattedDate)
        holder.tuitionStatus.text = setBoldLabel("Status: ", post.status)
        holder.tuitionCode.text = setBoldLabel("Tuition Code: ", post.tuitionCode?.toString() ?: "N/A")
        holder.classDetails.text = setBoldLabel("Class: ", post.className)
        holder.subjectDetails.text = setBoldLabel("Subject: ", post.subject)
        holder.boardDetails.text = setBoldLabel("Education Board: ", post.educationBoard)
        holder.feeDetails.text = setBoldLabel("Expected Fee: ", post.fee ?: "N/A")
        holder.classTiming.text = setBoldLabel("Time: ", post.classTiming)
        holder.classSchedule.text = setBoldLabel("Schedule: ", post.classSchedule)
        holder.locationDetails.text = setBoldLabel(
            "Address: ",
            "${post.sublocality}, ${post.area}, ${post.city}"
        )
        holder.genderPreference.text = setBoldLabel("Gender Preference: ", post.gender)
        holder.minQualification.text = setBoldLabel("Min. Qualification: ", post.qualification)
        holder.modeOfClass.text = setBoldLabel("Mode: ", post.modeOfClass)
        holder.demoClassDate.text = setBoldLabel("Demo Class Date: ", post.demoClassDate ?: "N/A")
        holder.specialRequirement.text = setBoldLabel("Special Req: ", post.specialReq ?: "None")

        holder.distanceDetails.text = post.distanceInKm?.let { km ->
            if (km < 1) {
                val meters = (km * 1000).roundToInt()
                setBoldLabel("Distance: ", "$meters m", Color.parseColor("#4CAF50")) // green
            } else {
                setBoldLabel("Distance: ", "${"%.2f".format(km)} km", Color.parseColor("#FFC107")) // material amber
            }
        } ?: run {
            post.distanceInMeters?.let { meters ->
                setBoldLabel("Distance: ", "${meters.roundToInt()} m", Color.parseColor("#4CAF50")) // green
            } ?: setBoldLabel("Distance: ", "N/A", Color.GRAY)
        }

        when (post.status) {
            "expired" -> {
                holder.layoutTuitionCard.alpha = 0.7f
                holder.expiredText.visibility = View.VISIBLE
                holder.tuitionStatus.text = setBoldLabel("Status: ", post.status, Color.parseColor("#F44336"))
                holder.applyBtn.isEnabled = false
                holder.favouriteBtn.isEnabled = false
            }
            "filled" -> {
                holder.layoutTuitionCard.alpha = 0.7f
                holder.filledText.visibility = View.VISIBLE
                holder.tuitionStatus.text = setBoldLabel("Status: ", post.status, Color.parseColor("#FFC107"))
                holder.applyBtn.isEnabled = false
                holder.favouriteBtn.isEnabled = false
            }
            "active" -> {
                holder.itemView.alpha = 1f
                holder.layoutTuitionCard.alpha = 1f
                holder.tuitionStatus.text = setBoldLabel("Status: ", post.status, Color.parseColor("#4CAF50"))
                holder.expiredText.visibility = View.GONE
                holder.filledText.visibility = View.GONE
                holder.applyBtn.isEnabled = true
                holder.favouriteBtn.isEnabled = true
            }
        }


        // Handle button clicks
        holder.applyBtn.setOnClickListener { onApplyClick(post) }
        holder.favouriteBtn.setOnClickListener { onFavouriteClick(post) }
    }

    override fun getItemCount() = items.size
}
