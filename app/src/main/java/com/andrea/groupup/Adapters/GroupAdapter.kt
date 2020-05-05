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

class GroupAdapter(items: ArrayList<Group>, ctx: Context) :
    ArrayAdapter<Group>(ctx,
        R.layout.list_of_groups, items) {

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

        val group = getItem(i)
        viewHolder.titleView!!.text = group!!.title
        viewHolder.idView!!.text = group.id.toString()
        viewHolder.numberOfPeople!!.text = group.id.toString() + " Users"
        if(group.image != 0){
            viewHolder.imageView!!.setImageResource(group.image)
        }

        view.tag = viewHolder

        return view
    }
}