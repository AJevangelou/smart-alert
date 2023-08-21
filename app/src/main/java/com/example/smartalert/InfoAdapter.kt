package com.example.smartalert

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class InfoAdapter(private val fireDataList: List<InfoData>) : RecyclerView.Adapter<InfoAdapter.ViewHolder>() {

    // ViewHolder class to hold the views
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize the views here
        // For example:
        val infoDesc: TextView = itemView.findViewById(R.id.infoDesc)
        val infoImage: ImageView = itemView.findViewById(R.id.infoImage)
        val lonText: TextView = itemView.findViewById(R.id.lonText)
        val latText: TextView = itemView.findViewById(R.id.latText)
        val credibilityImg: ImageView = itemView.findViewById(R.id.credibilityImg)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the layout for each item in the RecyclerView
        val view = LayoutInflater.from(parent.context).inflate(R.layout.info_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind the data to the views in each item
        val fireData = fireDataList[position]

        // For example:
        holder.infoDesc.text = fireData.description
        holder.lonText.text = fireData.longitude.toString()
        holder.latText.text = fireData.latitude.toString()

        if(fireData.count != null && fireData.count < 10) {
            holder.credibilityImg.setBackgroundResource(R.drawable.low_credibility)
        }else if(fireData.count != null && fireData.count < 20) {
            holder.credibilityImg.setBackgroundResource(R.drawable.medium_credibility)
        }else{
            holder.credibilityImg.setBackgroundResource(R.drawable.high_credibility)
        }


        when (fireData.path) {
            "fire" -> {
                holder.infoImage.setBackgroundResource(R.drawable.fire_button)
            }
            "earthquake" -> {
                holder.infoImage.setBackgroundResource(R.drawable.earthquake_button)
            }
            "flood" -> {
                holder.infoImage.setBackgroundResource(R.drawable.flood_button)
            }
            else -> {
                Log.d("Fire Data path", fireData.path.toString())
                holder.infoImage.setBackgroundResource(R.drawable.other_button)
            }
        }


        if (fireData.photoUrl != null) {
            Log.d("Fire Photo", fireData.photoUrl!!)
            holder.infoImage.setBackgroundResource(0)
            Picasso.get().load(fireData.photoUrl).into(holder.infoImage)
        }

        // Set onClick listener for the item view
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int {
        // Return the total number of items in the list
        Log.d("Number of items:", fireDataList.size.toString())
        return fireDataList.size
    }
}