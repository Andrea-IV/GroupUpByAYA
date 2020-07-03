package com.andrea.groupup.Fragments

import android.Manifest
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.andrea.groupup.Adapters.MessageAdapter
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.MemberData
import com.andrea.groupup.Models.Message
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.gson.Gson
import com.scaledrone.lib.Listener
import com.scaledrone.lib.Room
import com.scaledrone.lib.RoomListener
import com.scaledrone.lib.Scaledrone
import kotlinx.android.synthetic.main.activity_place.*
import kotlinx.android.synthetic.main.fragment_chat_map.*
import kotlinx.android.synthetic.main.fragment_chat_map.view.*
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MAP"

class ChatMapFragment : BaseFragment(), OnMapReadyCallback, OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, RoomListener {

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var onMapChatButton: ImageButton;
    private var mLocationPermissionGranted = false;

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var markers = ArrayList<Marker>()
    private var localPlaces = HashMap<Int, LocalPlace>()

    //chat variables

    private var chat:LinearLayout? = null;
    private var chatTextLayout:LinearLayout? = null;
    private var channelID: String? = "xFOd3Tqsb25TmL2e"
    private val roomName = "observable-room"
    private var editText: EditText? = null
    private var scaledrone: Scaledrone? = null
    private var messageAdapter: MessageAdapter? = null
    private var messagesView: ListView? = null
    private var onMap: Boolean = true
    private var chatlayoutparams: ViewGroup.LayoutParams? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
     ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat_map, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        var maplayoutparams = mapFragment.view?.layoutParams
        chat = view.findViewById(R.id.chat) as LinearLayout
        chatlayoutparams = chat?.layoutParams
        onMapChatButton = view.onMapChatButton


        chatTextLayout = view.findViewById(R.id.chatTextLayout) as LinearLayout
        editText = view.findViewById(R.id.editText) as EditText
        messageAdapter = MessageAdapter(ACTIVITY)
        messagesView = view.findViewById(R.id.messages_view) as ListView
        messagesView!!.adapter = messageAdapter
        val data = MemberData(getRandomName(), getRandomColor())
        scaledrone = Scaledrone(channelID, data)
        scaledrone!!.connect(object : Listener {
            override fun onOpen() {
                println("Scaledrone connection open")
                scaledrone!!.subscribe(roomName, this@ChatMapFragment)
            }

            override fun onOpenFailure(ex: java.lang.Exception) {
                System.err.println(ex)
            }

            override fun onFailure(ex: java.lang.Exception) {
                System.err.println(ex)
            }

            override fun onClosed(reason: String) {
                System.err.println(reason)
            }
        })

        onMapChatButton.setOnClickListener { view ->
            //opacity/background brinfront constraint affichebarretexte booleen visibiltytext
            if(onMap)
            {
                bringChat()
            }
            else
            {
                bringMap()
            }
        }

        return view
    }

    private fun getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())

        try {
            if(mLocationPermissionGranted) {
                val location = this.mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful) {
                        Log.d(TAG, "onComplete: found location")
                        val current = it.result
                        println("move please")
                        if(current !== null)
                            moveCamera(LatLng(current.latitude, current.longitude), 9.5f);
                    } else {
                        Log.d(TAG, "onComplete: location not found")
                        Toast.makeText(this.activity, "Can't get your current location", Toast.LENGTH_SHORT).show()
                    }
                })
            }

        } catch (e: SecurityException) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
        }
    }

    private fun checkGps(): Boolean {
        val lm: LocationManager = context!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false;
        try {
            return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (e: Exception) {
            Log.e(TAG, "askForGps: " + e.message)
        }

        return gpsEnabled
    }

    private fun askForGps() {
        Log.d(TAG, "askForGps")

        val dialog = AlertDialog
            .Builder(this.context)
            .setMessage("Please turn on GPS Location")
            .setCancelable(false)
            .setPositiveButton("Done", DialogInterface.OnClickListener {
                _, _-> run {
                    if(checkGps()) getDeviceLocation()
                }
            })
            .create()

        dialog.setTitle("Gps error")
        dialog.show()
    }

    private fun getLocalPlaces(target: LatLng) {
        Log.d(TAG, "getLocalPlaces + " + target.latitude + " " + target.longitude)
        LocalPlaceHttp(this.requireContext()).getByLatLng(target.latitude.toString(), target.longitude.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d(TAG, "getLocalPlaces - onResponse")
                Log.d(TAG, array.toString())
                val lpRes = Mapper().mapper<JSONArray, List<LocalPlace>>(array)
                for(localPlace in lpRes) {
                    localPlaces.put(localPlace.id, localPlace)
                    addMarker(localPlace.id, localPlace.name, localPlace.opening_hour + " - " + localPlace.closing_hour, LatLng(localPlace.coordinate_x.toDouble(), localPlace.coordinate_y.toDouble()), false)
                }
            }

            override fun onError(error: VolleyError): Unit {
                Log.e(TAG, "getLocalPlaces - onError")
                Log.e(TAG, error.javaClass.toString())
            }
        })
    }

    private fun addMarker(id: Int, title: String, snippet: String, latLng: LatLng, show: Boolean){
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
        )

        marker.hideInfoWindow()
        marker.tag = id
        markers.add(marker)
    }

    private fun removeMarkers() {
        markers.forEach {
            it.remove()
        }

        markers.clear()
    }

    private fun displayLocalPlace(marker: Marker) {
        Log.d(TAG, "displayLocalPlace " + marker.title)
        val localPlace = localPlaces.get(marker.tag)
        println(localPlace)
        val intent = Intent(activity, PlaceActivity::class.java)
        intent.putExtra("localPlace", Gson().toJson(localPlace))
        startActivity(intent)
    }
    // -------------------------

    override fun onMapReady(gMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        mMap = gMap
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnCameraIdleListener(this)
        mMap.setOnMarkerClickListener(this)

        if(checkPermission()) {
            mLocationPermissionGranted = true
            getDeviceLocation();
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissions()
        }

        //this.mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick")
        displayLocalPlace(marker)
        return true
    }
    override fun onCameraIdle() {
        Log.d(TAG, "OnCameraIdle = " + mMap.cameraPosition.zoom)
        Log.d(TAG, "OnCameraIdle = " + mMap.cameraPosition.target.latitude + " " + mMap.cameraPosition.target.longitude)
        if(mMap.cameraPosition.zoom >= 9.5f) {
            getLocalPlaces(mMap.cameraPosition.target)
        } else {
            removeMarkers()
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        Log.d(TAG, "Moving camera to lat " + latLng.latitude + ", lng " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun checkPermission() : Boolean {
        Log.d(TAG, "checkPermission")
        return (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        Log.d(TAG, "requestPermissions")
        ActivityCompat.requestPermissions(this.requireActivity(), this.permissions,1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult")
        if(requestCode == 1) {
            if(grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if(it != PackageManager.PERMISSION_GRANTED) {
                        mLocationPermissionGranted = false
                        return
                    }
                }
                mLocationPermissionGranted = true;
                getDeviceLocation()
                mMap.isMyLocationEnabled = true
            }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        Log.d(TAG, "onMyLocationButtonClick")

        if(!checkGps()) {
            askForGps()
        } else {
            getDeviceLocation()
        }

        return true
    }

    //CHAT FUNCTIONS
    //------------------------

    fun sendMessage(view: View?) {
        val message = editText!!.text.toString()
        if (message.length > 0) {
            scaledrone!!.publish(roomName, message)
            editText!!.text.clear()
        }
    }

    override fun onOpen(room: Room?) {
        println("Conneted to room")
    }

    override fun onOpenFailure(room: Room?, ex: Exception?) {
        System.err.println(ex)
    }

    override fun onMessage(room: Room?, receivedMessage: com.scaledrone.lib.Message) {
        val mapper = ObjectMapper()
        try {
            val data = mapper.treeToValue(
                receivedMessage.member.clientData,
                MemberData::class.java
            )
            val belongsToCurrentUser =
                receivedMessage.clientID == scaledrone!!.clientID
            val message = Message(
                receivedMessage.data.asText(),
                data,
                belongsToCurrentUser
            )
            //runOnUiThread {
                messageAdapter!!.add(message)
                messagesView!!.setSelection(messagesView!!.count - 1)
            //}
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }

    private fun getRandomName(): String? {
        val adjs = arrayOf(
            "autumn",
            "hidden",
            "bitter",
            "misty",
            "silent",
            "empty",
            "dry",
            "dark",
            "summer",
            "icy",
            "delicate",
            "quiet",
            "white",
            "cool",
            "spring",
            "winter",
            "patient",
            "twilight",
            "dawn",
            "crimson",
            "wispy",
            "weathered",
            "blue",
            "billowing",
            "broken",
            "cold",
            "damp",
            "falling",
            "frosty",
            "green",
            "long",
            "late",
            "lingering",
            "bold",
            "little",
            "morning",
            "muddy",
            "old",
            "red",
            "rough",
            "still",
            "small",
            "sparkling",
            "throbbing",
            "shy",
            "wandering",
            "withered",
            "wild",
            "black",
            "young",
            "holy",
            "solitary",
            "fragrant",
            "aged",
            "snowy",
            "proud",
            "floral",
            "restless",
            "divine",
            "polished",
            "ancient",
            "purple",
            "lively",
            "nameless"
        )
        val nouns = arrayOf(
            "waterfall",
            "river",
            "breeze",
            "moon",
            "rain",
            "wind",
            "sea",
            "morning",
            "snow",
            "lake",
            "sunset",
            "pine",
            "shadow",
            "leaf",
            "dawn",
            "glitter",
            "forest",
            "hill",
            "cloud",
            "meadow",
            "sun",
            "glade",
            "bird",
            "brook",
            "butterfly",
            "bush",
            "dew",
            "dust",
            "field",
            "fire",
            "flower",
            "firefly",
            "feather",
            "grass",
            "haze",
            "mountain",
            "night",
            "pond",
            "darkness",
            "snowflake",
            "silence",
            "sound",
            "sky",
            "shape",
            "surf",
            "thunder",
            "violet",
            "water",
            "wildflower",
            "wave",
            "water",
            "resonance",
            "sun",
            "wood",
            "dream",
            "cherry",
            "tree",
            "fog",
            "frost",
            "voice",
            "paper",
            "frog",
            "smoke",
            "star"
        )
        return adjs[Math.floor(Math.random() * adjs.size).toInt()] +
                "_" +
                nouns[Math.floor(Math.random() * nouns.size).toInt()]
    }

    private fun getRandomColor(): String? {
        val r = Random()
        val sb = StringBuffer("#")
        while (sb.length < 7) {
            sb.append(Integer.toHexString(r.nextInt()))
        }
        return sb.toString().substring(0, 7)
    }

    //CHAT-MAP SWITCH FUNCTIONS
    //------------------------

    private fun bringChat(){
        chat?.layoutParams =  ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        chat?.setBackgroundColor(Color.parseColor("#ffffffff"))
        chatTextLayout?.setVisibility(View.VISIBLE)
        mapFragment.view?.layoutParams = chatlayoutparams
        mapFragment.view?.bringToFront()
        onMap = false
    }

    private fun bringMap(){
        mapFragment.view?.layoutParams =  ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        mapFragment.view?.setBackgroundColor(Color.parseColor("#ffffffff"))
        chat?.setBackgroundColor(Color.parseColor("#90FFFFFF"))
        chatTextLayout?.setVisibility(View.GONE)
        chat?.layoutParams = chatlayoutparams
        chat?.bringToFront()
        onMap = true
    }
}
