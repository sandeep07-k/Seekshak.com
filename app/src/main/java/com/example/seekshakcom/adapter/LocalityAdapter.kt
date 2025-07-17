package com.example.seekshakcom.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.R

class LocalityAdapter(
    private val localityList: List<String>,
    private val onLocalityClick: (String) -> Unit
) : RecyclerView.Adapter<LocalityAdapter.LocalityViewHolder>() {

    inner class LocalityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val localityName: TextView = itemView.findViewById(R.id.localityName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocalityViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_locality, parent, false)
        return LocalityViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocalityViewHolder, position: Int) {
        val locality = localityList[position]
        holder.localityName.text = locality
        holder.itemView.setOnClickListener {
            onLocalityClick(locality)
        }
    }

    override fun getItemCount(): Int = localityList.size
}
