package com.andrea.groupup.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.andrea.groupup.Constants
import com.andrea.groupup.Http.EventHttp
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.Event
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.User
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class PlanAdapter(var events: ArrayList<Event>, var selectedAdapter: SelectedAdapter, var items: ArrayList<LocalPlace>, val event: Event, val user: User, val group: Group, val token: String, val ctx: Context) :
    RecyclerView.Adapter<PlanAdapter.PlaceViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        return PlaceViewHolder(LayoutInflater.from(ctx).inflate(R.layout.list_of_selected, parent, false), token, group, user, events, selectedAdapter, event)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val itemPlace = items[position]
        holder.bindPlace(itemPlace, this)
    }

    class PlaceViewHolder(v: View, val token: String, val group: Group, val user: User, var events: ArrayList<Event>, var selectedAdapter: SelectedAdapter, val event: Event) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var plan: LocalPlace? = null
        private var planAdapter: PlanAdapter? = null

        private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            getDeviceLocation(plan!!.id)
        }

        fun bindPlace(place: LocalPlace, planAdapter: PlanAdapter) {
            this.plan = place
            this.planAdapter = planAdapter

            (view.findViewById<View>(R.id.title) as TextView).text = plan?.name

            if(plan?.Ratings != null){
                (view.findViewById<View>(R.id.rating) as TextView).text = plan?.Ratings.toString()
            }else{
                (view.findViewById<View>(R.id.rating) as TextView).text = "No ratings"
            }

            Picasso.get().load(Constants.BASE_URL + "/" + place.Photos[0].link).into(view.findViewById<ImageView>(R.id.image))

            if(event.UserId == user.id){
                (view.findViewById<View>(R.id.deletePlan) as ImageView).setOnClickListener {
                    deleteSelector(plan!!)
                }
            }else{
                (view.findViewById<View>(R.id.deletePlan) as ImageView).visibility = View.GONE
            }
        }

        private fun findLocalPlaces(location: Location?, id: Int) {
            if (location != null) {
                LocalPlaceHttp(itemView.context).getAllInfoLocalplace(group.id.toString(), location.latitude.toString(), location.longitude.toString(), token, object:
                    VolleyCallbackArray {
                    override fun onResponse(array: JSONArray) {
                        val lpRes = Mapper().mapper<JSONArray, List<LocalPlace>>(array)
                        for(localPlace in lpRes) {
                            if(localPlace.id == id){
                                goToPlace(localPlace)
                            }
                        }
                    }

                    override fun onError(error: VolleyError): Unit {
                        Log.e("LOCALPLACE", "getLocalPlaces - onError")
                    }
                })
            }
        }

        private fun goToPlace(localPlace: LocalPlace){
            val intent = Intent(itemView.context, PlaceActivity::class.java).apply {

                putExtra("PLACE", localPlace)
                putExtra("GROUP", group)
                putExtra("USER", user)
                putExtra("TOKEN", token)
            }
            itemView.context.startActivity(intent)
        }

        private fun getDeviceLocation(id: Int) {
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(itemView.context)
            mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
                findLocalPlaces(location, id)
            }
        }

        private fun deleteSelector(plan: LocalPlace){
            if(event.LocalPlaces.size < 2){
                deleteEvent()
            }else{
                deletePlan(plan)
            }
        }

        private fun deletePlan(plan: LocalPlace){
            event.LocalPlaces.remove(plan)
            for(i: Int in 0 until event.LocalPlaces.size){
                event.LocalPlaces[i].pos = i + 1
            }
            EventHttp(itemView.context).deletePlan(event, token, object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d("EVENT", jsonObject.toString())
                    planAdapter?.notifyDataSetChanged()
                }

                override fun onError(error: VolleyError): Unit {
                    Log.e("EVENTS", "Event - onError")
                    Log.e("EVENTS", error.toString())
                    planAdapter?.notifyDataSetChanged()
                }
            })
        }

        private fun deleteEvent(){
            EventHttp(itemView.context).deleteEvent(event, token, object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d("EVENT", jsonObject.toString())
                }

                override fun onError(error: VolleyError): Unit {
                    Log.e("EVENTS", "Event - onError")
                    Log.e("EVENTS", error.toString())
                    events.remove(event)
                    if(events.isEmpty()){
                        (itemView.context as Activity).finish()
                    }else{
                        selectedAdapter.items.remove(user)
                        selectedAdapter.notifyDataSetChanged()
                    }
                }
            })
        }
    }
}