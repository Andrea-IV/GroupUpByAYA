package com.andrea.groupup.Fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andrea.groupup.Adapters.MessageAdapter
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.TravelHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.MemberData
import com.andrea.groupup.Models.Message
import com.andrea.groupup.Models.Travel
import com.andrea.groupup.Models.User
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.scaledrone.lib.*
import kotlinx.android.synthetic.main.fragment_chat_map.view.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MAP"

class ChatMapFragment : BaseFragment(), OnMapReadyCallback, /*OnMyLocationButtonClickListener,*/ GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, RoomListener {

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    lateinit var group: Group
    lateinit var user: User
    lateinit var token: String

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap

    private var mLocationPermissionGranted = false;

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var localPlacesMarkers = ArrayList<Marker>()
    private var localPlaces = emptyList<LocalPlace>()

    private var todaysTravel: Travel? = null
    private var actualTravel: Travel? = null
    private var travelMarkers = ArrayList<Marker>()
    private lateinit var actualPolyline: Polyline
    private var isTravelDisplayed = true

    private lateinit var meetingPointsHandler: Handler

    private val checkClockForMeetingPoints = object: Runnable {
        override fun run() {
            displayMeetingPoints()
            meetingPointsHandler.postDelayed(this, 60000) // every minutes
        }
    }

    //chat variables
    private lateinit var onMapChatButton: ImageButton;
    private lateinit var sendMessage: ImageButton;
    private lateinit var mapHideButton: ImageButton;


    private var chat:LinearLayout? = null;
    private var chatTextLayout:LinearLayout? = null;
    private var relativeChatLayout:RelativeLayout? = null;
    private var channelID: String? = null
    private var roomName: String? = null
    private var editText: EditText? = null
    private var scaledrone: Scaledrone? = null
    private var messageAdapter: MessageAdapter? = null
    private var messagesView: ListView? = null
    private var chatlayoutparams: ViewGroup.LayoutParams? = null
    private var maplayoutparams: ViewGroup.LayoutParams? = null
    private var data : MemberData? = null
    private var onMap: Boolean = true
    private var onLittleMap: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
     ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat_map, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        maplayoutparams = mapFragment.view?.layoutParams
        chat = view.findViewById(R.id.chat) as LinearLayout
        chatlayoutparams = chat?.layoutParams
        onMapChatButton = view.onMapChatButton
        sendMessage = view.sendMessage
        mapHideButton = view.mapHideButton


        chatTextLayout = view.findViewById(R.id.chatTextLayout) as LinearLayout
        relativeChatLayout = view.findViewById(R.id.relativeChatLayout) as RelativeLayout
        editText = view.findViewById(R.id.editText) as EditText
        messageAdapter = MessageAdapter(ACTIVITY)
        messagesView = view.findViewById(R.id.messages_view) as ListView
        messagesView!!.adapter = messageAdapter
        data = MemberData(ACTIVITY.user.username, getRandomColor())
        channelID = getString(R.string.chat_channel)
        roomName = "observable-"+ACTIVITY.group.name+"___"+ACTIVITY.group.id
        scaledrone = Scaledrone(channelID, data)
        scaledrone!!.connect(object : Listener {
            override fun onOpen() {
                println("Scaledrone connection open")
                scaledrone!!.subscribe(roomName, this@ChatMapFragment, SubscribeOptions(100)).listenToHistoryEvents { room, message ->
                   onMessage(room, message)
                    //println(message)
                }
            }

            override fun onOpenFailure(ex: java.lang.Exception) {
                System.err.println("sauce"+ex)
            }

            override fun onFailure(ex: java.lang.Exception) {
                System.err.println("sauce"+ex)
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
        sendMessage.setOnClickListener { view ->
            sendMessage(view)
        }

        mapHideButton.setOnClickListener{view ->
            if(onLittleMap)
            {
                hideLittleMap()
            }
            else
            {
                bringLittleMap()
            }
        }

        view.findViewById<FloatingActionButton>(R.id.myLocationButton).setOnClickListener {
            Log.d(TAG, "onMyLocationButtonClick")

            if(!checkGps()) {
                askForGps()
            } else {
                getDeviceLocation()
            }
        }

        view.findViewById<FloatingActionButton>(R.id.myTravelButton).setOnClickListener {
            Log.d(TAG, "myTravelButtonClick")

            if(isTravelDisplayed) {
                removeMarkers(travelMarkers)
                actualPolyline.remove()
                setLocalPlacesMarkers()
            } else {
                removeMarkers(localPlacesMarkers)
                displayActualTravel()
            }

            isTravelDisplayed = !isTravelDisplayed
        }

        group = ACTIVITY.group
        user = ACTIVITY.user
        token = ACTIVITY.token

        meetingPointsHandler = Handler(Looper.getMainLooper())
        return view
    }

    override fun onPause() {
        super.onPause()
        meetingPointsHandler.removeCallbacks(checkClockForMeetingPoints)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "ITS RESUMING")
        meetingPointsHandler.post(checkClockForMeetingPoints)
    }

    fun displayMeetingPoints() {

    }

    private fun getTodaysTravel() {
        TravelHttp(ACTIVITY).getTodaysTravel(group.id, token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d(TAG, "getTodaysTravel On Response")

                val gson: Gson = Gson()
                todaysTravel = gson.fromJson(jsonObject.toString(), Travel::class.java)
                actualTravel = todaysTravel
                displayActualTravel()
                println(todaysTravel)
            }

            override fun onError(error: VolleyError) {
                Log.e(TAG, "getTodaysTravel On Error")
                Log.e(TAG, error.toString())
                isTravelDisplayed = false
            }
        })
    }

    private fun getTodaysMeetingpoints() {

    }

    private fun displayActualTravel() {
        if (actualTravel !== null) {
            setTravelMarkers(actualTravel!!.LocalPlaces)
            generateTravelPolyline()
        }
    }

    private fun setTravelMarkers(localplaces: List<LocalPlace>) {
        localplaces.forEach {
            var bitmap = BitmapDescriptorFactory.fromResource(R.drawable.blue_pushpin)
            if (actualTravel !== null) {
                if (it.TravelLocalplace?.position == 1) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.purple_pushpin)
                } else if(it.TravelLocalplace?.position == localplaces.size) {
                    bitmap = BitmapDescriptorFactory.fromResource(R.drawable.red_pushpin)
                }
            }

            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(LatLng(it.coordinate_x.toDouble(), it.coordinate_y.toDouble()))
                    .title(it.name)
                    .icon(bitmap)

            )

            marker.tag = it.id
            travelMarkers.add(marker)
        }
    }

    private fun generateTravelPolyline() {
        val polylineOption = PolylineOptions()
        actualTravel?.LocalPlaces?.sortBy { it.TravelLocalplace?.position }
        actualTravel?.LocalPlaces?.forEach {
            Log.d(TAG, "i'm here")
            polylineOption.add(LatLng(it.coordinate_x.toDouble(), it.coordinate_y.toDouble()))
        }
        polylineOption.width(4f)
        actualPolyline = mMap.addPolyline(polylineOption)
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
        val lm: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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
        Log.d(TAG, "getLocalPlaces = " + target.latitude + " " + target.longitude)
        LocalPlaceHttp(this.requireContext()).getByLatLng(target.latitude.toString(), target.longitude.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d(TAG, "getLocalPlaces - onResponse")
                Log.d(TAG, array.toString())
                localPlaces = Mapper().mapper<JSONArray, List<LocalPlace>>(array)
                if(!isTravelDisplayed)
                    setLocalPlacesMarkers()
            }

            override fun onError(error: VolleyError): Unit {
                Log.e(TAG, "getLocalPlaces - onError")
                Log.e(TAG, error.javaClass.toString())
            }
        })
    }

    private fun setLocalPlacesMarkers() {
        localPlaces.forEach {
            addMarker(it.id, it.name, it.opening_hour + " - " + it.closing_hour, LatLng(it.coordinate_x.toDouble(), it.coordinate_y.toDouble()), false)
        }
    }

    private fun isLocalPlaceInActualTravel(id: Int): Boolean {
        actualTravel!!.LocalPlaces.forEach {
            if(it.id == id)
                return true
        }

        return false
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
        localPlacesMarkers.add(marker)
    }

    private fun removeMarkers(markerList: ArrayList<Marker>) {
        markerList.forEach {
            it.remove()
        }

        markerList.clear()
    }

    private fun displayLocalPlace(marker: Marker) {
        Log.d(TAG, "displayLocalPlace " + marker.title)
        val localPlace = localPlaces.filter { it.id == marker.tag }
        println(localPlace)
        val intent = Intent(activity, PlaceActivity::class.java)
        intent.putExtra("localPlace", Gson().toJson(localPlace))
        startActivity(intent)
    }
    // -------------------------

    val mapClickEvent = object : GoogleMap.OnMapClickListener {
        override fun onMapClick(p0: LatLng?) {
            Log.d(TAG, "OnMapClickListener ${p0?.latitude} ${p0?.longitude}")

            val marker = mMap.addMarker(
                MarkerOptions()
                .position(p0!!)
                .title("Ajouter un point de rassemblement")
            )
            marker.tag = "meetingpoint"
        }
    }

    override fun onMapReady(gMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        mMap = gMap
        mMap.uiSettings.isMyLocationButtonEnabled = false;
        mMap.setOnCameraIdleListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(mapClickEvent)

        if(checkPermission()) {
            mLocationPermissionGranted = true
            getDeviceLocation();
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissions()
        }

        getTodaysTravel()
        //this.mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick")
        if (marker.tag === "meetingpoint") {
            displayMeetingPointCreateActivity()
        } else {
            displayLocalPlace(marker)
        }

        return true
    }

    override fun onCameraIdle() {
        Log.d(TAG, "OnCameraIdle = " + mMap.cameraPosition.zoom)
        Log.d(TAG, "OnCameraIdle = " + mMap.cameraPosition.target.latitude + " " + mMap.cameraPosition.target.longitude)
        if(mMap.cameraPosition.zoom >= 9.5f) {
            getLocalPlaces(mMap.cameraPosition.target)
        } else {
            removeMarkers(localPlacesMarkers)
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        Log.d(TAG, "Moving camera to lat " + latLng.latitude + ", lng " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    private fun checkPermission() : Boolean {
        Log.d(TAG, "checkPermission")
        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
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

    fun displayMeetingPointCreateActivity() {

    }

    //CHAT FUNCTIONS
    //------------------------

    fun sendMessage(view: View?) {
        val message = Message(
            editText!!.text.toString(),
            data,
            true
        )
        if (message.text.length > 0) {
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
                receivedMessage.data,
                Message::class.java
            )
            val belongsToCurrentUser =
                data.memberData.name == this.data?.name
            val message = Message(
                data.text,
                data.memberData,
                belongsToCurrentUser
            )
            ACTIVITY.runOnUiThread {
                messageAdapter!!.add(message)
                messagesView!!.setSelection(messagesView!!.count - 1)
            }
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
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

        //afficher bouton affichage, slide sur bouton affichage,
        //
        mapFragment.view?.alpha = 0.5f
        view?.myLocationButton?.hide()
        view?.myTravelButton?.hide()
        mapFragment.view?.bringToFront()
        onMap = false
    }

    private fun bringMap(){
        mapFragment.view?.layoutParams =   ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT)
        mapFragment.view?.setBackgroundColor(Color.parseColor("#ffffffff"))
        chat?.setBackgroundColor(Color.parseColor("#90FFFFFF"))
        chatTextLayout?.visibility = View.GONE
        chat?.layoutParams = chatlayoutparams
        mapFragment.view?.alpha = 1f
        view?.myLocationButton?.show()
        view?.myTravelButton?.show()
        chat?.bringToFront()
        onMap = true
    }



    private fun bringLittleMap(){
        mapFragment.view?.visibility = View.VISIBLE
        relativeChatLayout?.visibility = View.VISIBLE
        onLittleMap = true
    }



    private fun hideLittleMap(){
        mapFragment.view?.visibility = View.GONE
        relativeChatLayout?.visibility = View.GONE
        onLittleMap = false
    }
}
