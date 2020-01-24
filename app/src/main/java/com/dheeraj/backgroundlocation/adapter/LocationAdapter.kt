package com.dheeraj.backgroundlocation.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.dheeraj.backgroundlocation.R
import com.dheeraj.backgroundlocation.interfaces.OnItemClickListener
import com.dheeraj.backgroundlocation.model.LocationModel
import kotlinx.android.synthetic.main.item_location.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LocationAdapter(
    private val locationData: ArrayList<LocationModel>,
    private val itemClickListener: OnItemClickListener
) :
    RecyclerView.Adapter<LocationAdapter.MainIngViewHolder>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): MainIngViewHolder {
        val view = LayoutInflater.from(p0.context).inflate(R.layout.item_location, p0, false)
        return MainIngViewHolder(view)
    }

    override fun getItemCount(): Int {
        return locationData.size
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    override fun onBindViewHolder(holder: MainIngViewHolder, position: Int) {
        holder.tvNo.text        = (position+1).toString()+". "
        holder.tvLat.text       = locationData[position].lat.toString()
        holder.tvLongi.text     = locationData[position].longi.toString()
        holder.tvDateTime.text  = getDateTime(locationData[position].timestamp)

        holder.rootView.setOnClickListener {
            itemClickListener.onItemClick(position, locationData[position])
        }
    }

    class MainIngViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var rootView = itemView.root_view as ConstraintLayout
        var tvLat          = itemView.tvLat as TextView
        var tvLongi        = itemView.tvLongi as TextView
        var tvDateTime     = itemView.tvDateTime as TextView
        var tvNo           = itemView.tvNo as TextView
    }

    private fun getDateTime(s: Long): String? {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault())
            val netDate = Date(s)
            sdf.format(netDate)
        } catch (e: Exception) {
            e.toString()
        }
    }
}
