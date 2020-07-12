package com.andrea.groupup.Adapters

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import com.andrea.groupup.Constants
import com.andrea.groupup.EventActivity
import com.andrea.groupup.Models.EventDisplay
import com.andrea.groupup.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class EventAdapter(items: ArrayList<EventDisplay>, ctx: Context) :
    ArrayAdapter<EventDisplay>(ctx,
        R.layout.list_of_events, items) {

    var arrayList = items
    var tempList = ArrayList<EventDisplay>()
    var done = false

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

            if(user.pp_link.contains("base")){
                img.setImageResource(R.drawable.example)
            }else{
                Picasso.get().load(Constants.BASE_URL + "/" + user.pp_link).into(img)
            }

            viewHolder.linearLayout?.addView(img)
        }

        view.tag = viewHolder

        return view
    }

    fun displayOnlyDate(date: String){
        if(!done){
            tempList.addAll(arrayList)
            done = true
        }
        arrayList.clear()

        if (date.isEmpty()) {
            arrayList.addAll(tempList)
        } else {
            for (i in 0 until tempList.size) {
                Log.d("DATE", tempList[i].date)
                if (tempList[i].date!!.toLowerCase(Locale.getDefault()).contains(date)) {
                    arrayList.add(tempList[i])
                }
            }
        }
        notifyDataSetChanged()
    }

    fun filter(text: String?) {
        val text = text!!.toLowerCase(Locale.getDefault())

    }
}