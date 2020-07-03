package com.andrea.groupup.Fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.TravelHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.Models.Travel
import com.andrea.groupup.Models.User
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.android.volley.VolleyError
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
import org.json.JSONArray
import org.json.JSONObject

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MAP"

class ChatMapFragment : BaseFragment(), OnMapReadyCallback, /*OnMyLocationButtonClickListener,*/ GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat_map, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        setTravelMarkers(actualTravel!!.LocalPlaces)
        generateTravelPolyline()
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

    fun displayMeetingPointCreateActivity() {

    }
}
