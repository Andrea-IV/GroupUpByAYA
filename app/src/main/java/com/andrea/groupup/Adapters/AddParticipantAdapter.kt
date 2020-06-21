package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.andrea.groupup.Models.User
import com.andrea.groupup.R
import java.util.*
import kotlin.collections.ArrayList

class AddParticipantAdapter(items: ArrayList<User>, ctx: Context) :
    ArrayAdapter<User>(ctx,
        R.layout.list_of_add_participants, items) {

    var arrayList = items
    var tempList = ArrayList(arrayList)

    //view holder is used to prevent findViewById calls
    private class ParticipantViewHolder {
        internal var image: ImageView? = null
        internal var name: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: ParticipantViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_add_participants, viewGroup, false)

            viewHolder = ParticipantViewHolder()
            viewHolder.image = view!!.findViewById<View>(R.id.profile_image) as ImageView
            viewHolder.name = view.findViewById<View>(R.id.name) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as ParticipantViewHolder
        }

        val participant = getItem(i)
        //viewHolder.image!!.src = event!!.date
        viewHolder.name!!.text = participant.username

        view.tag = viewHolder

        return view
    }

    fun filter(text: String?) {
        val text = text!!.toLowerCase(Locale.getDefault())
        arrayList.clear()

        if (text.length == 0) {
            arrayList.addAll(tempList)
        } else {
            for (i in 0..tempList.size - 1) {
                if (tempList[i].username!!.toLowerCase(Locale.getDefault()).contains(text)) {
                    arrayList.add(tempList.get(i))
                }
            }
        }

        notifyDataSetChanged()
    }
}