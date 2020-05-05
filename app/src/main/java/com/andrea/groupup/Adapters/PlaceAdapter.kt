package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Models.Place
import com.andrea.groupup.R

class PlaceAdapter(private val places: ArrayList<Place>, private val ctx: Context) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(LayoutInflater.from(ctx).inflate(R.layout.list_of_places, parent, false))
    }

    override fun getItemCount() = places.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val itemPlace = places[position]
        holder.bindPlace(itemPlace)
    }

    class PlaceHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var place: Place? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            // TODO
        }

        fun bindPlace(place: Place) {
            this.place = place
            (view.findViewById<View>(R.id.title) as TextView).text = place.title
            if(place.image % 2 != 0){
                (view.findViewById<View>(R.id.image) as ImageView).setImageResource(R.drawable.place_example_2)
            }
        }

    }

}