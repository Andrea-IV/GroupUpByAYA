package com.andrea.groupup.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Constants
import com.andrea.groupup.Models.Event
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlaceComparator
import com.andrea.groupup.Models.User
import com.andrea.groupup.R
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class SelectedAdapter(var items: ArrayList<User>, val userReceived: User, val group: Group, val token: String, var events: ArrayList<Event>, var ctx: Context) :
    ArrayAdapter<User>(ctx,
        R.layout.list_of_user_events, items) {

    //view holder is used to prevent findViewById calls
    private class SelectedViewHolder {
        internal var name: TextView? = null
        internal var listOfPlace: RecyclerView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: SelectedViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(ctx)
            view = inflater.inflate(R.layout.list_of_user_events, viewGroup, false)

            viewHolder = SelectedViewHolder()
            viewHolder.name = view!!.findViewById<View>(R.id.name) as TextView
            viewHolder.listOfPlace = view.findViewById<View>(R.id.listOfPlace) as RecyclerView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as SelectedViewHolder
        }

        val user = getItem(i)
        val title = user!!.username + context.resources.getString(R.string.plan_name)
        viewHolder.name!!.text = title

        Log.d("USER", user.toString())

        if(!user!!.pp_link.contains("base")){
            Picasso.get().load(Constants.BASE_URL + "/" + user.pp_link).into(view.findViewById<CircleImageView>(R.id.profile_image))
        }else{
            view.findViewById<CircleImageView>(R.id.profile_image).setImageResource(R.drawable.example)
        }

        for(event in events){
            if(event.UserId == user.id){
                Collections.sort(event.LocalPlaces, LocalPlaceComparator())
                val adapterPlace = PlanAdapter(events, this, event.LocalPlaces, event, userReceived, group, token, ctx)
                viewHolder.listOfPlace!!.layoutManager = LinearLayoutManager(ctx)
                viewHolder.listOfPlace!!.adapter = adapterPlace
                val itemDecorator = DividerItemDecoration(ctx, DividerItemDecoration.VERTICAL)
                itemDecorator.setDrawable(ContextCompat.getDrawable(ctx, R.drawable.divider)!!)
                viewHolder.listOfPlace!!.addItemDecoration(itemDecorator)

                val params: ViewGroup.LayoutParams = viewHolder.listOfPlace!!.layoutParams
                val scale = context.resources.displayMetrics.density
                params.height = ((100 * scale + 0.5f).toInt() * event.LocalPlaces.size + 1) + ((5 * scale + 0.5f).toInt() * event.LocalPlaces.size + 1)
                viewHolder.listOfPlace!!.layoutParams = params

                break
            }
        }

        view.tag = viewHolder

        return view
    }
}