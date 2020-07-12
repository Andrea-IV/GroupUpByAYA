package com.andrea.groupup.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.andrea.groupup.Adapters.PlaceAdapter
import com.andrea.groupup.CreatePlaceActivity
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.Place
import com.andrea.groupup.R
import com.andrea.groupup.token
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import org.json.JSONArray
import java.util.*
import kotlin.collections.HashMap


/**
 * A simple [Fragment] subclass.
 */
class ExploreFragment : BaseFragment() {
    private lateinit var adapter: PlaceAdapter
    private lateinit var layoutManager: StaggeredGridLayoutManager
    private var listItems = arrayListOf<LocalPlace>()
    private var allListItems = arrayListOf<LocalPlace>()
    private lateinit var searchView: SearchView
    private val ctx: ExploreFragment = this

    lateinit var token: String
    lateinit var group: Group
    lateinit var mView: View

    private val localPlaceInfoRunnable =  object: Runnable {
        override fun run() {
            getDeviceLocation(true)
            infoLocalPlaceHandler.postDelayed(this, 5000)
        }
    }
    private lateinit var infoLocalPlaceHandler: Handler

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_explore, container, false)

        group = ACTIVITY.group
        token = ACTIVITY.token

        getDeviceLocation(false)
        searchInit()

        mView.findViewById<ImageView>(R.id.filter).setOnClickListener {
            initMenu(it)
        }

        infoLocalPlaceHandler = Handler(Looper.getMainLooper())
        infoLocalPlaceHandler.post(localPlaceInfoRunnable)

        return mView
    }

    override fun onResume() {
        super.onResume()
        infoLocalPlaceHandler.post(localPlaceInfoRunnable)
    }

    override fun onPause() {
        super.onPause()
        infoLocalPlaceHandler.removeCallbacks(localPlaceInfoRunnable)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        allListItems.clear()
        listItems.clear()
        getDeviceLocation(false)
    }

    private fun initMenu(it: View){
        val popupMenu = PopupMenu(this.requireContext(), it)
        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.shor -> {
                    listItems.clear()
                    for(place in allListItems){
                        if(place.distance!! < 2.0){
                            listItems.add(place)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    true
                }
                R.id.mid -> {
                    listItems.clear()
                    for(place in allListItems){
                        if(place.distance!! < 5.0){
                            listItems.add(place)
                        }
                    }
                    adapter.notifyDataSetChanged()
                    true
                }
                R.id.all -> {
                    listItems.clear()
                    for(place in allListItems){
                        listItems.add(place)
                    }
                    adapter.notifyDataSetChanged()
                    true
                }
                else -> false
            }
        }

        popupMenu.inflate(R.menu.menu_explore)

        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popupMenu)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception){
            Log.e("Main", "Error showing menu icons.", e)
        } finally {
            popupMenu.show()
        }
    }

    private fun initLocalPlaces(location: Location?, verify: Boolean) {
        if (location != null) {
            LocalPlaceHttp(this.requireContext()).getAllInfoLocalplace(group.id.toString(), location.latitude.toString(), location.longitude.toString(), token, object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("LOCALPLACE", array.toString())
                    val lpRes = Mapper().mapper<JSONArray, List<LocalPlace>>(array)
                    if(!verify){
                        setLocalPlaceDisplay(lpRes)
                    }else if(!allListItems.equals(lpRes)){
                        setLocalPlaceDisplay(lpRes)
                    }
                }

                override fun onError(error: VolleyError): Unit {
                    Log.e("LOCALPLACE", "getLocalPlaces - onError")
                }
            })
        }else{
            LocalPlaceHttp(this.requireContext()).getAll(object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("LOCALPLACE", array.toString())
                    val lpRes = Mapper().mapper<JSONArray, List<LocalPlace>>(array)
                    if(!verify){
                        setLocalPlaceDisplay(lpRes)
                    }else if(!allListItems.equals(lpRes)){
                        setLocalPlaceDisplay(lpRes)
                    }
                }

                override fun onError(error: VolleyError): Unit {
                    Log.e("LOCALPLACE", "getLocalPlaces - onError")
                }
            })
        }
    }

    private fun getDeviceLocation(verify: Boolean) {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mFusedLocationProviderClient.lastLocation.addOnSuccessListener { location : Location? ->
            initLocalPlaces(location, verify)
        }
    }

    private fun setLocalPlaceDisplay(lpRes: List<LocalPlace>){
        allListItems.clear()
        listItems.clear()
        for(localPlace in lpRes) {
            listItems.add(localPlace)
            allListItems.add(localPlace)
        }

        layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        val recyclerView: RecyclerView = mView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager

        adapter = PlaceAdapter(listItems, token, group, ACTIVITY.user, layoutManager, requireContext(), ctx)
        recyclerView.adapter = adapter
    }

    private fun searchInit(){
        searchView = mView.findViewById(R.id.searchBar)
        searchView.queryHint = getString(R.string.searchHint)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter(newText)
                return false
            }
        })
    }
}
