package com.andrea.groupup.Adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.andrea.groupup.EventActivity
import com.andrea.groupup.Models.EventDisplay
import com.andrea.groupup.R
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventAdapter(items: ArrayList<EventDisplay>, ctx: Context) :
    ArrayAdapter<EventDisplay>(ctx,
        R.layout.list_of_events, items) {

    //view holder is used to prevent findViewById calls
    private class EventViewHolder {
        internal var date: TextView? = null
        internal var linearLayout: LinearLayout? = null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: EventViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_events, viewGroup, false)

            viewHolder = EventViewHolder()
            viewHolder.date = view!!.findViewById<View>(R.id.date) as TextView
            viewHolder.linearLayout = view.findViewById<View>(R.id.linearLayout) as LinearLayout
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as EventViewHolder
        }

        val event = getItem(i)

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(event!!.date, formatter)

        val final_formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

        viewHolder.date!!.text = date.format(final_formatter).toString()
        viewHolder.linearLayout?.removeAllViews()
        for (user in event.users){
            val img = CircleImageView(context)
            val scale = context.resources.displayMetrics.density
            val params = LinearLayout.LayoutParams((40 * scale + 0.5f).toInt(), (40 * scale + 0.5f).toInt())
            params.setMargins(0,0,10,0)
            img.layoutParams = params
            img.borderWidth = 2
            img.setImageResource(R.drawable.example)

            viewHolder.linearLayout?.addView(img)
        }

        view.tag = viewHolder

        return view
    }
}