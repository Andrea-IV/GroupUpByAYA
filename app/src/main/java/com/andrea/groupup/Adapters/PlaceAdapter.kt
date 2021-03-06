package com.andrea.groupup.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.andrea.groupup.Constants
import com.andrea.groupup.Fragments.ExploreFragment
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.User
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

const val PLACE_STRING = "PLACE"
class PlaceAdapter(private val places: ArrayList<LocalPlace>, val token: String, val group: Group, val user:User, var layoutManager: StaggeredGridLayoutManager, private val ctx: Context, val frag: ExploreFragment) : RecyclerView.Adapter<PlaceAdapter.PlaceHolder>()  {

    var arrayList = places
    var tempList = ArrayList<LocalPlace>()
    var done = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(LayoutInflater.from(ctx).inflate(R.layout.list_of_places, parent, false), token, group, user, frag)
    }

    override fun getItemCount() = places.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val itemPlace = places[position]
        holder.bindPlace(itemPlace, layoutManager, this)
    }

    class PlaceHolder(v: View, val token: String, val group: Group, val user: User, val frag: ExploreFragment) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var place: LocalPlace? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val context = itemView.context
            println("PLACE = UWU")
            println(place)
            val intent = Intent(context, PlaceActivity::class.java).apply {
                putExtra(PLACE_STRING, place)
                putExtra("GROUP", group)
                putExtra("USER", user)
                putExtra("TOKEN", token)
            }

            frag.startActivityForResult(intent, 0);
        }

        fun bindPlace(place: LocalPlace, layoutManager: StaggeredGridLayoutManager, placeAdapter: PlaceAdapter) {
            this.place = place
            (view.findViewById<View>(R.id.title) as TextView).text = place.name
            if(place.Photos.isNotEmpty()){
                Picasso.get().load(Constants.BASE_URL + "/" + place.Photos[0].link).into(view.findViewById<ImageView>(R.id.image))
                layoutManager.invalidateSpanAssignments()
            }
        }

    }

    fun filter(text: String?) {
        if(!done){
            tempList.addAll(arrayList)
            done = true
        }
        val text = text!!.toLowerCase(Locale.getDefault())
        arrayList.clear()

        if (text.isEmpty()) {
            arrayList.addAll(tempList)
        } else {
            for (i in 0 until tempList.size) {
                if (tempList[i].name.toLowerCase(Locale.getDefault()).contains(text)) {
                    arrayList.add(tempList[i])
                }else{
                    for (j in 0 until tempList[i].Tags.size){
                        if (tempList[i].Tags[j].name.toLowerCase(Locale.getDefault()).contains(text)) {
                            arrayList.add(tempList[i])
                            break
                        }
                    }
                }
            }
        }

        notifyDataSetChanged()
    }
}