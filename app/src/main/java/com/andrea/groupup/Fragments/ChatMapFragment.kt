package com.andrea.groupup.Fragments

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.Fragment
import com.andrea.groupup.*
import com.andrea.groupup.Adapters.MessageAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.*
import com.andrea.groupup.Models.Message
import com.andrea.groupup.Models.Notification
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.beust.klaxon.json
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.common.api.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.scaledrone.lib.*
import com.squareup.picasso.Picasso
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

class ChatMapFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener, RoomListener, MultiplePermissionsListener {

    private var permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

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
    private var actualPolyline: Polyline? = null
    private var isTravelDisplayed = true
    private var createMeetingPointMarker: Marker? = null
    private lateinit var meetingPointsList: List<MeetingPoint>
    private var meetingPointMarkerList = ArrayList<Marker>()

    private lateinit var sharePositionHandler: Handler
    private val checkPositionShareStateRunnable =  object: Runnable {
        override fun run() {
            checkUserPositionShareState()
            sharePositionHandler.postDelayed(this, 1000)
        }
    }

    private lateinit var friendsLocationHandler: Handler
    private val getFriendsLocationRunnable = object: Runnable {
        override fun run() {
            getFriendsLocation()
            friendsLocationHandler.postDelayed(this, 1000)
        }

    }

    private lateinit var shareLocationButton: ImageButton
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
    private var isHistory: Boolean = false

    private lateinit var preferences: SharedPreferences
    private lateinit var edit: SharedPreferences.Editor

    private lateinit var serviceIntent: Intent

    private var friendsBitmap = HashMap<Int, Bitmap?>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
     ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat_map, container, false)

        group = ACTIVITY.group
        user = ACTIVITY.user
        token = ACTIVITY.token

        group.members.forEach {
            Log.d("PICASSO", "${Constants.BASE_URL}/${it.pp_link}")
            Picasso.get().load("${Constants.BASE_URL}/${it.pp_link}").into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    // loaded bitmap is here (bitmap)
                    Log.d(TAG, "onBitmapLoaded -> ${it.username}")
                    friendsBitmap[it.id] = bitmap
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.d(TAG, "onBitmapFailed -> ${it.username}")
                }
            })
        }

        preferences = ACTIVITY.getSharedPreferences("groupup", Context.MODE_PRIVATE)
        edit = preferences.edit()

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        maplayoutparams = mapFragment.view?.layoutParams
        chat = view.findViewById(R.id.chat) as LinearLayout
        chatlayoutparams = chat?.layoutParams
        onMapChatButton = view.onMapChatButton
        sendMessage = view.sendMessage
        mapHideButton = view.mapHideButton
        shareLocationButton = view.findViewById(R.id.shareLocationButton)

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
                    isHistory = true
                   onMessage(room, message)
                    isHistory = false
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
            sendMessage()
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

            Dexter.withActivity(ACTIVITY)
                .withPermissions(permissions)
                .withListener(this)
                .check()
        }

        view.findViewById<FloatingActionButton>(R.id.myTravelButton).setOnClickListener {
            Log.d(TAG, "myTravelButtonClick")

            if(isTravelDisplayed) {
                removeMarkers(travelMarkers)
                actualPolyline?.remove()
                setLocalPlacesMarkers()
            } else {
                removeMarkers(localPlacesMarkers)
                displayActualTravel()
            }

            isTravelDisplayed = !isTravelDisplayed
        }

        view.findViewById<FloatingActionButton>(R.id.myChatButton).setOnClickListener {
            bringChat()
        }

        shareLocationButton.setOnClickListener {
            Log.d(TAG, "issharing = " + preferences.getBoolean("isSharing", false).toString())
            if (!preferences.getBoolean("isSharing", false)) {
                edit.putBoolean("isSharing", true)
                edit.apply()
                serviceIntent = Intent(ACTIVITY, SharePositionService::class.java)
                serviceIntent.putExtra("groupId", group.id)
                serviceIntent.putExtra("groupName", group.name)
                serviceIntent.putExtra("user", user)
                serviceIntent.putExtra("token", token)
                ACTIVITY.startService(serviceIntent)
            } else {
                if(this::serviceIntent.isInitialized)
                    ACTIVITY.stopService(serviceIntent)
                edit.putBoolean("isSharing", false)
                edit.apply()
            }
        }

        if(checkGps()) {
            shareLocationButton.visibility = View.VISIBLE
        }


        sharePositionHandler = Handler(Looper.getMainLooper())
        sharePositionHandler.post(checkPositionShareStateRunnable)
        friendsLocationHandler = Handler(Looper.getMainLooper())
        friendsLocationHandler.post(getFriendsLocationRunnable)
        return view
    }

    private fun getFriendsLocation() {
        Log.d(TAG, "getFriendsLocation")
        GroupHttp(ACTIVITY)
            .getById(group.id, object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d(TAG, "getFriendsLocation - onResponse")
                    group = Mapper().mapper(jsonObject)
                    displayFriendsLocation()
                }

                override fun onError(error: VolleyError) {
                    Log.d(TAG, "getFriendsLocation - onError")
                    Log.e(TAG, error.toString())
                }

            })
    }


    private var friendsMarker = ArrayList<Marker>()
    private fun displayFriendsLocation() {
//        removeMarkers(friendsMarker)
        group.members.forEach {
            if(it.id != user.id && it.UserGroup.is_sharing_pos) {
                var bDesc: BitmapDescriptor? = null
                if (friendsBitmap[it.id] != null) {
                    bDesc = BitmapDescriptorFactory.fromBitmap(friendsBitmap[it.id])
                }
                val fm = getFriendMarker(it.username)
                if(fm !== null) {
                    fm.position = LatLng(it.UserGroup.coordinate_x.toDouble(), it.UserGroup.coordinate_y.toDouble())
                    fm.setIcon(bDesc)
                } else {
                    friendsMarker.add(
                        addMarker(
                            it.username,
                            null,
                            "friend " + it.id,
                            LatLng(
                                it.UserGroup.coordinate_x.toDouble(),
                                it.UserGroup.coordinate_y.toDouble()
                            ),
                            bDesc,
                            false
                        )
                    )
                }
            } else {
                removeFriendMarker(it.username)
            }
        }
    }

    private fun getFriendMarker(username: String): Marker? {
        friendsMarker.forEach {
            if(it.title == username)
                return it
        }

        return null
    }

    private fun removeFriendMarker(username: String) {
        friendsMarker.forEach {
            if(it.title == username)
                it.remove()
        }
    }

    private fun checkUserPositionShareState() {
        Log.d(TAG, "checkUserPositionShareState")
        Log.d(TAG, preferences.getBoolean("isSharing", false).toString())
        if(preferences.getBoolean("isSharing", false)) {
            DrawableCompat.setTint(DrawableCompat.wrap(shareLocationButton.background), context?.resources!!.getColor(R.color.sharePositionButtonStart))
        } else {
            DrawableCompat.setTint(DrawableCompat.wrap(shareLocationButton.background), Color.parseColor("#FFFFFF"))
            sharePositionHandler.removeCallbacks(checkPositionShareStateRunnable)
        }
    }


    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>, token: PermissionToken) {
        Log.d(TAG, "onPermissionRationaleShouldBeShown")
        // This method will be called when the user rejects a permission request
        // You must display a dialog box that explains to the user why the application needs this permission
        createDialolg(
            "Gps permission error",
            "Please go to your application settings and allow location share so you can share your location with your friends !",
            "Accept",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                token.continuePermissionRequest()
            },
            "Refuse",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.dismiss()
                token.cancelPermissionRequest()
            }
        )
    }

    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
        Log.d(TAG, "onPermissionsChecked")
        // Here you have to check granted permissions
        if (report.areAllPermissionsGranted()) {
            mMap.isMyLocationEnabled = true
            checkLocationEnabled()
        }
    }

    override fun onStart() {
        super.onStart()
        context?.registerReceiver(gpsBroadcastReceiver, IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION))
    }
    override fun onPause() {
        super.onPause()
//        meetingPointsHandler.removeCallbacks(checkClockForMeetingPoints)
    }

    override fun onStop() {
        super.onStop()
        context?.unregisterReceiver(gpsBroadcastReceiver)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
//        meetingPointsHandler.post(checkClockForMeetingPoints)
        if(checkGps()) getDeviceLocation()
        getMeetingPointsNow()
    }

    private val gpsBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            Log.d(TAG, "gpsBroadcastReceiver")
            Log.d("INTENT", intent?.action)
            when(intent?.action) {
                "android.location.PROVIDERS_CHANGED" -> {
                    Log.d("INTENT2", "uwu")
                    if(checkGps()) {
                        Log.d(TAG, "gpsBroadcastReceiver - onReceive - gps enabled")
                        shareLocationButton.visibility = View.VISIBLE
                    } else {
                        Log.d(TAG, "gpsBroadcastReceiver - onReceive - gps disabled")
                        shareLocationButton.visibility = View.GONE
                    }
                }
            }
        }
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

            travelMarkers.add(addMarker(it.name, null, it.id.toString(), LatLng(it.coordinate_x.toDouble(), it.coordinate_y.toDouble()), bitmap, false))
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

    private fun checkLocationEnabled() {
        if(checkGps()) {
            shareLocationButton.visibility = View.VISIBLE
            getDeviceLocation()
        } else {
            createDialolg(
                "Gps network is not enabled",
                "Please turn on gps sor you can ",
                "Open location settings",
                DialogInterface.OnClickListener { _, _ ->
                    startActivityForResult(Intent(ACTION_LOCATION_SOURCE_SETTINGS), 2);
                },
                "Cancel",
                null
            )
        }
    }

    private fun createDialolg(title: String, message: String, positive: String, positiveClick: DialogInterface.OnClickListener?, negative: String, negativeClick: DialogInterface.OnClickListener?) {
        AlertDialog
            .Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton(positive, positiveClick)
            .setNegativeButton(negative, negativeClick)
            .show()
    }
    private fun getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation")
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
        mFusedLocationProviderClient.locationAvailability.addOnSuccessListener {
            Log.d(TAG, "onSuccess: found location")
            val current = it
            if (current !== null) {
                if (current.isLocationAvailable) {
                    mFusedLocationProviderClient.lastLocation.addOnSuccessListener {
                        moveCamera(LatLng(it.latitude, it.longitude), 9.5f)
                    }
                } else {
                    checkLocationEnabled()
                }
            }
        }
    }

    private fun getLocationAvailability(callback: Task<LocationAvailability>) {

    }

    private fun checkGps(): Boolean {
        Log.d(TAG, "checkGps")
        val lm = ACTIVITY.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gps_enabled = false;
        var network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(e: Exception) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(e: Exception) {}

        return gps_enabled && network_enabled
    }

    private fun getLocalPlaces(target: LatLng) {
        Log.d(TAG, "getLocalPlaces = " + target.latitude + " " + target.longitude)
        LocalPlaceHttp(this.requireContext()).getByLatLngAndTrad(target.latitude.toString(), target.longitude.toString(), Locale.getDefault().language, object: VolleyCallbackArray {
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
            localPlacesMarkers.add(addMarker(
                it.name,
                it.opening_hour + " - " + it.closing_hour,
                it.id.toString(),
                LatLng(it.coordinate_x.toDouble(), it.coordinate_y.toDouble()),
                null,
                false))
        }
    }

    private fun isLocalPlaceInActualTravel(id: Int): Boolean {
        actualTravel!!.LocalPlaces.forEach {
            if(it.id == id)
                return true
        }

        return false
    }

    fun addMarker(title: String?, snippet: String?, tag: String?, latLng: LatLng, bitmap: BitmapDescriptor?, showInfoWindow: Boolean): Marker {
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet)
                .icon(bitmap)
        )

        if(showInfoWindow)
            marker.showInfoWindow()
        else
            marker.hideInfoWindow()

        marker.tag = tag
        return marker
    }

    private fun removeMarkers(markerList: ArrayList<Marker>) {
        markerList.forEach {
            it.remove()
        }

        markerList.clear()
    }

    private fun displayLocalPlace(marker: Marker) {
        Log.d(TAG, "displayLocalPlace " + marker.tag)
        val localPlace = localPlaces.filter { it.id == marker.tag.toString().toInt() }[0]
        println(localPlace)
        val intent = Intent(context, PlaceActivity::class.java).apply {
            putExtra("PLACE", localPlace)
            putExtra("TOKEN", ACTIVITY.token)
            putExtra("FROM", "map")
            putExtra("GROUP", ACTIVITY.group)
            putExtra("USER", ACTIVITY.user)
        }
        startActivity(intent)
    }


    private val mapClickEvent = GoogleMap.OnMapClickListener { p0 ->
        Log.d(TAG, "OnMapClickListener ${p0?.latitude} ${p0?.longitude}")

        createMeetingPointMarker?.remove()
        createMeetingPointMarker = addMarker("Ajouter un point de rassemblement", null, "create_meeting_point", p0!!, null, true)
    }

    override fun onMapReady(gMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        mMap = gMap
        mMap.uiSettings.isMyLocationButtonEnabled = false;
        mMap.setOnCameraIdleListener(this)
        mMap.setOnMarkerClickListener(this)
        mMap.setOnMapClickListener(mapClickEvent)

//        if(checkPermission()) {
//            mLocationPermissionGranted = true
//            mMap.isMyLocationEnabled = true

//            getDeviceLocation();
//        } else {
//            requestPermissions()
//        }

        getTodaysTravel()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        Log.d(TAG, "onMarkerClick")
        println("marker = " + marker.tag.toString().split(" ")[0])
        if (marker.tag == "create_meeting_point") {
            displayMeetingPointCreateActivity(marker)
        } else if (marker.tag.toString().split(" ")[0] == "show_meeting_point") {
            displayMeetingPointShowActivity(marker)
        } else if (marker.tag.toString().split(" ")[0] == "friend") {
            marker.showInfoWindow()
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

//    private fun checkPermission() : Boolean {
//        Log.d(TAG, "checkPermission")
//        return (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
//    }

//    private fun requestPermissions() {
//        Log.d(TAG, "requestPermissions")
//        ActivityCompat.requestPermissions(this.requireActivity(), this.permissions,1)
//    }

//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        Log.d(TAG, "onRequestPermissionsResult")
//        if(requestCode == 1) {
//            if(grantResults.isNotEmpty()) {
//                grantResults.forEach {
//                    if(it != PackageManager.PERMISSION_GRANTED) {
//                        mLocationPermissionGranted = false
//                        return
//                    }
//                }
//                mLocationPermissionGranted = true;
//                mMap.isMyLocationEnabled = true
//                getDeviceLocation()
//
//            }
//        }
//    }

    private fun displayMeetingPointCreateActivity(marker: Marker) {
        Log.d(TAG, "displayMeetingPointCreateActivity")
        val intent = Intent(context, CreateMeetingPointActivity::class.java)
//        intent.putExtra("meetingPoing", user)
        intent.putExtra("latlng", marker.position)
        intent.putExtra("user", user)
        intent.putExtra("token", token)
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            1 -> {
                Log.d(TAG, "onActivityResult - 1")
                if(resultCode == 1) {
                    createMeetingPointMarker?.remove()
                    createMeetingPointMarker = null
                    sendNotifications("Meeting notification", "Your friend " + user.username + " asks you to join him !", null)
                } else if (resultCode == 0) {
                    createMeetingPointMarker?.showInfoWindow()
                }
            }
        }
    }

    fun getMeetingPointsNow() {
        Log.d(TAG, "getMeetingPointsNow")
        MeetingPointHttp(ACTIVITY).getNow(group.id, object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d(TAG, "getMeetingPointsNow - onResponse")
                meetingPointsList = Mapper().mapper(array)
                Log.d("MEETING POINTS ARRAY", meetingPointsList.toString())
                displayMeetingPoints()
            }

            override fun onError(error: VolleyError) {
                Log.d(TAG, "getMeetingPointsNow - onError")
                Log.e(TAG, error.toString())
            }
        })
    }

    fun displayMeetingPoints() {
        removeMarkers(meetingPointMarkerList)

        val bitmap = BitmapDescriptorFactory.fromResource(R.drawable.white_pushpin)

        meetingPointsList.forEach {meetingPoint ->
            val user = group.members.filter { it.id == meetingPoint.UserId }[0]
            meetingPointMarkerList.add(addMarker(user.username, null, "show_meeting_point " + meetingPoint.id.toString(), LatLng(meetingPoint.coordinate_x.toDouble(), meetingPoint.coordinate_y.toDouble()), bitmap, true))
        }

    }

    private fun displayMeetingPointShowActivity(marker: Marker) {
        Log.d(TAG, "displayMeetingPointShowActivity")
        val intent = Intent(context, ShowMeetingPointActivity::class.java)
        val mp = meetingPointsList.filter { it.id == marker.tag.toString().split(" ")[1].toInt() }[0]
        intent.putExtra("meetingpoint", mp)
        intent.putExtra("token", token)
        val creator = group.members.filter { it.id == mp.UserId }[0]
        intent.putExtra("user", creator)
        intent.putExtra("creator", creator.id == user.id)
        startActivity(intent)
    }

    //CHAT FUNCTIONS
    //------------------------

    fun sendMessage() {
        val message = Message(
            editText!!.text.toString(),
            data,
            true
        )
        if (message.text.length > 0) {
            sendNotifications("New Chat Message in group : "+group.name, message.text, "notif_group_"+group.id)
            scaledrone!!.publish(roomName, message)
            editText!!.text.clear()
        }
    }

    fun sendApplicationMessage(message: String) {
        val message = Message(
            message,
            data,
            true
        )
        if (message.text.length > 0) {
            scaledrone!!.publish(roomName, message)
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
            if(!isHistory && !belongsToCurrentUser){
                view?.myChatButton?.setImageResource(R.drawable.chat_notif);
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
        mapFragment.view?.alpha = 0.5f
        view?.myLocationButton?.hide()
        view?.myTravelButton?.hide()
        view?.myChatButton?.hide()
        mapFragment.view?.bringToFront()
        view?.myChatButton?.setImageResource(R.drawable.chat);
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
        view?.myChatButton?.show()
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

    private fun sendNotifications(title: String, message: String, tag: String?){
        NotificationHttp(ACTIVITY).send(
            Notification(
                group.id,
                NotificationMessage(
                    Notif(
                        title,
                        message,
                        tag
                    )
                )
            ),
            token,
            object : VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d(TAG, "sendNotifications - NotificationHttp - onResponse")
                }

                override fun onError(error: VolleyError) {
                    Log.d(TAG, "sendNotifications - NotificationHttp - onError")
                    Log.e(TAG, error.toString())
                }

            }
        )
    }
}