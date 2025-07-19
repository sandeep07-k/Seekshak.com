package com.example.seekshakcom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R
import com.example.seekshakcom.model.LocationRequest

class RecentLocationAdapter(
    private val onItemClick: (LocationRequest) -> Unit
) : RecyclerView.Adapter<RecentLocationAdapter.LocationViewHolder>() {

    private val locations: MutableList<LocationRequest> = mutableListOf()

    fun setLocations(newLocations: List<LocationRequest>) {
        locations.clear()
        locations.addAll(newLocations)
        notifyDataSetChanged()
    }

    fun getLocations(): List<LocationRequest> = locations

    fun removeLocation(position: Int) {
        if (position in locations.indices) {
            locations.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.bind(location)
        holder.itemView.setOnClickListener {
            onItemClick(location)
        }
    }

    override fun getItemCount(): Int = locations.size

    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val locationText: TextView = itemView.findViewById(R.id.recentLocationText)

        fun bind(location: LocationRequest) {
            locationText.text = listOfNotNull(location.sublocality,location.area, location.city)
                .joinToString(", ")
        }
    }
}
