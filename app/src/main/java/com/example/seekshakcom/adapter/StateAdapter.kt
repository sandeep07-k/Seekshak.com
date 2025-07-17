package com.example.seekshakcom.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.seekshakcom.CitySelectorActivity
import com.example.seekshakcom.R

class StateAdapter(
    private val context: Context,
    private val states: List<String>,
    private val onStateClick: (String) -> Unit
) : RecyclerView.Adapter<StateAdapter.StateViewHolder>() {

    inner class StateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val stateName: TextView = itemView.findViewById(R.id.stateName)
        val arrowIcon: ImageView = itemView.findViewById(R.id.arrowIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_state, parent, false)
        return StateViewHolder(view)
    }

    override fun onBindViewHolder(holder: StateViewHolder, position: Int) {
        val state = states[position]
        holder.stateName.text = state

        holder.itemView.setOnClickListener {
            onStateClick(state)

            // Directly start CitySelectorActivity like OLX
            val intent = Intent(context, CitySelectorActivity::class.java)
            intent.putExtra("selected_state", state)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = states.size
}
