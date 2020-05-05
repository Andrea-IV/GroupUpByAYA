package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.Place
import com.andrea.groupup.R

class PlaceAdapter(items: ArrayList<Place>, ctx: Context) :
    ArrayAdapter<Place>(ctx,
        R.layout.list_of_places, items) {

    //view holder is used to prevent findViewById calls
    private class PlaceViewHolder {
        internal var imageView: ImageView? = null
        internal var titleView: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var mView = view

        val viewHolder: PlaceViewHolder

        if (mView == null) {
            val inflater = LayoutInflater.from(context)
            mView = inflater.inflate(R.layout.list_of_places, viewGroup, false)

            viewHolder = PlaceViewHolder()
            viewHolder.imageView = mView!!.findViewById<View>(R.id.image) as ImageView
            viewHolder.titleView = mView.findViewById<View>(R.id.title) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = mView.tag as PlaceViewHolder
        }

        val place = getItem(i)
        viewHolder.titleView!!.text = place!!.title

        if(place.image != 0){
            viewHolder.imageView!!.setImageResource(place.image)
        }

        mView.tag = viewHolder

        return mView
    }
}