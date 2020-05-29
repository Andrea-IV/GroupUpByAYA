package com.andrea.groupup.Fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.andrea.groupup.DetailsActivity
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.LocalPlace
import com.andrea.groupup.PlaceActivity
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.beust.klaxon.JsonArray
import com.beust.klaxon.Klaxon
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
import org.json.JSONArray

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MAP"

class ChatMapFragment : Fragment(), OnMapReadyCallback, OnMyLocationButtonClickListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener {

    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted = false;

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

    private var markers = ArrayList<Marker>()
    private var localPlaces = HashMap<Int, LocalPlace>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_chat_map, container, false)

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
}
