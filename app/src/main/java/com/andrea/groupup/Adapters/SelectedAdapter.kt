package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.andrea.groupup.Models.Event
import com.andrea.groupup.Models.Place
import com.andrea.groupup.R

class SelectedAdapter(items: ArrayList<Place>, ctx: Context) :
    ArrayAdapter<Place>(ctx,
        R.layout.list_of_selected, items) {

    //view holder is used to prevent findViewById calls
    private class SelectedViewHolder {
        internal var title: TextView? = null
        internal var location: TextView? = null
        internal var rating: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: SelectedViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_selected, viewGroup, false)

            viewHolder = SelectedViewHolder()
            viewHolder.title = view!!.findViewById<View>(R.id.title) as TextView
            viewHolder.location = view.findViewById<View>(R.id.location) as TextView
            viewHolder.rating = view.findViewById<View>(R.id.rating) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as SelectedViewHolder
        }

        val place = getItem(i)
        viewHolder.title!!.text = place!!.title
        viewHolder.location!!.text = place.location
        viewHolder.rating!!.text = place.rating.toString()

        view.tag = viewHolder

        return view
    }
}