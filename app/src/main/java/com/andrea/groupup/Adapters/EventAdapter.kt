package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.andrea.groupup.Models.Event
import com.andrea.groupup.R

class EventAdapter(items: ArrayList<Event>, ctx: Context) :
    ArrayAdapter<Event>(ctx,
        R.layout.list_of_groups, items) {

    //view holder is used to prevent findViewById calls
    private class EventViewHolder {
        internal var date: TextView? = null
        internal var location: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: EventViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_events, viewGroup, false)

            viewHolder = EventViewHolder()
            viewHolder.date = view!!.findViewById<View>(R.id.date) as TextView
            viewHolder.location = view.findViewById<View>(R.id.location) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as EventViewHolder
        }

        val event = getItem(i)
        viewHolder.date!!.text = event!!.date
        viewHolder.location!!.text = event.location

        view.tag = viewHolder

        return view
    }
}