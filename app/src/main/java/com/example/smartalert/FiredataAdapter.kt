package com.example.smartalert

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView

class FiredataAdapter(private val fireDataList: List<FirebaseData>) : RecyclerView.Adapter<FiredataAdapter.ViewHolder>() {

    // ViewHolder class to hold the views
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize the views here
        // For example:
        val firebaseDesc: TextView = itemView.findViewById(R.id.firebaseDesc)
        val firebaseImage: ImageView = itemView.findViewById(R.id.firebaseImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_fire_data, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind the data to the views in each item
        val fireData = fireDataList[position]

        // For example:
        holder.firebaseDesc.text = fireData.description[0]
        holder.firebaseImage.setImageResource(fireData.image)

        // Set onClick listener for the item view
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, InfoActivity::class.java)
            intent.putExtra("key", fireData.key)
            intent.putExtra("country", fireData.country)
            intent.putExtra("city", fireData.city)
            intent.putExtra("path", fireData.path)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        return fireDataList.size
    }
}
