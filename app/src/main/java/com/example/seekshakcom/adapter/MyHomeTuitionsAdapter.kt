package com.example.seekshakcom.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.TuitionPost
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Locale
import com.example.seekshakcom.utils.TextUtilsHelper.setBoldLabel

class MyHomeTuitionsAdapter(
    private val items: MutableList<TuitionPost>,
    private val showViewAll: Boolean = false,
    private val onApplyClick: (TuitionPost) -> Unit,
    private val onFavouriteClick: (TuitionPost) -> Unit,
    private val onViewAllClick: (() -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_VIEW_ALL = 1
    private val VIEW_TYPE_LOADING = 2

    private var isLoadingFooterVisible = false

    /** Show or hide bottom shimmer loader */
    fun showLoadingFooter(show: Boolean) {
        if (show == isLoadingFooterVisible) return
        isLoadingFooterVisible = show
        if (show) notifyItemInserted(items.size) else notifyItemRemoved(items.size)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            showViewAll && position == items.size -> VIEW_TYPE_VIEW_ALL
            isLoadingFooterVisible && position == items.size -> VIEW_TYPE_LOADING
            else -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_ITEM -> TuitionViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_tuition_card, parent, false)
            )
            VIEW_TYPE_VIEW_ALL -> ViewAllViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_view_all_button, parent, false)
            )
            VIEW_TYPE_LOADING -> ShimmerViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.item_shimmer_footer, parent, false)
            )
            else -> throw IllegalArgumentException("Unknown view type $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TuitionViewHolder -> {
                // Ensure we don't access out-of-bounds
                if (position < items.size) bindTuition(holder, items[position])
            }
            is ViewAllViewHolder -> holder.btnViewAll.setOnClickListener { onViewAllClick?.invoke() }
            is ShimmerViewHolder -> {} // shimmer auto-animates
        }
    }

    override fun getItemCount(): Int {
        var count = items.size
        if (showViewAll) count += 1
        if (isLoadingFooterVisible) count += 1
        return count
    }
    fun updateItems(newItems: List<TuitionPost>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }


    /** ViewHolders */
    inner class TuitionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    inner class ViewAllViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnViewAll: ImageButton = itemView.findViewById(R.id.btnViewAll)
    }

    inner class ShimmerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    /** Bind tuition card safely */
    private fun bindTuition(holder: TuitionViewHolder, post: TuitionPost) {
        val inputFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd-MMM-yy", Locale.getDefault())
        val formattedDate = try {
            val date = inputFormat.parse(post.postedDate)
            outputFormat.format(date)
        } catch (e: Exception) { post.postedDate ?: "N/A" }

        holder.postingDate.text = setBoldLabel("Posting date: ", formattedDate)
        holder.tuitionStatus.text = setBoldLabel("Status: ", post.status)
        holder.tuitionCode.text = setBoldLabel(
            "Tuition Code: ",
            post.tuitionCode?.toString() ?: "N/A"
        )
        holder.classDetails.text = setBoldLabel("Class: ", post.className)
        holder.subjectDetails.text = setBoldLabel("Subject: ", post.subject)
        holder.boardDetails.text = setBoldLabel("Education Board: ", post.educationBoard)
        holder.feeDetails.text = setBoldLabel("Expected Fee: ", post.fee ?: "N/A")
        holder.classTiming.text = setBoldLabel("Time: ", post.classTiming)
        holder.classSchedule.text = setBoldLabel("Schedule: ", post.classSchedule)
        holder.locationDetails.text = setBoldLabel("Address: ", "${post.sublocality}, ${post.area}, ${post.city}")
        holder.genderPreference.text = setBoldLabel("Gender Preference: ", post.gender)
        holder.minQualification.text = setBoldLabel("Min. Qualification: ", post.qualification)
        holder.modeOfClass.text = setBoldLabel("Mode: ", post.modeOfClass)
        holder.demoClassDate.text = setBoldLabel("Demo Class Date: ", post.demoClassDate ?: "N/A")
        holder.specialRequirement.text = setBoldLabel("Special Req: ", post.specialReq ?: "None")

        holder.distanceDetails.text = post.distanceInKm?.let { km ->
            if (km < 1) {
                val meters = (km * 1000).roundToInt()
                setBoldLabel("Distance: ", "$meters m", Color.parseColor("#4CAF50"))
            } else {
                setBoldLabel("Distance: ", "${"%.2f".format(km)} km", Color.parseColor("#FFC107"))
            }
        } ?: run {
            post.distanceInMeters?.let { meters ->
                setBoldLabel("Distance: ", "${meters.roundToInt()} m", Color.parseColor("#4CAF50"))
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



        holder.applyBtn.setOnClickListener { onApplyClick(post) }
        holder.favouriteBtn.setOnClickListener { onFavouriteClick(post) }
    }
}
