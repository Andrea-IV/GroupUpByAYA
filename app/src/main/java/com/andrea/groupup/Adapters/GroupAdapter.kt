package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.andrea.groupup.Models.Group
import com.andrea.groupup.R
import java.util.*
import kotlin.collections.ArrayList

class GroupAdapter(items: ArrayList<Group>, ctx: Context) :
    ArrayAdapter<Group>(ctx,
        R.layout.list_of_groups, items) {

    var arrayList = items
    var tempList = ArrayList(arrayList)

    //view holder is used to prevent findViewById calls
    private class GroupViewHolder {
        internal var imageView: ImageView? = null
        internal var titleView: TextView? = null
        internal var numberOfPeople: TextView? = null
        internal var idView: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: GroupViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_groups, viewGroup, false)

            viewHolder = GroupViewHolder()
            viewHolder.imageView = view!!.findViewById<View>(R.id.profile_image) as ImageView
            viewHolder.titleView = view.findViewById<View>(R.id.userTitle) as TextView
            viewHolder.numberOfPeople = view.findViewById<View>(R.id.people) as TextView
            viewHolder.idView = view.findViewById<View>(R.id.userId) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as GroupViewHolder
        }

        val group = arrayList.get(i)
        viewHolder.titleView!!.text = group!!.name
        viewHolder.idView!!.text = group.id.toString()
        val text = group.members.size.toString() + context.resources.getString(R.string.numberOfPeople)
        viewHolder.numberOfPeople!!.text = text
        /* if(group.image != 0){
             viewHolder.imageView!!.setImageResource(group.image)
         }*/

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
                if (tempList[i].name!!.toLowerCase(Locale.getDefault()).contains(text)) {
                    arrayList.add(tempList.get(i))
                }
            }
        }

        notifyDataSetChanged()
    }
}