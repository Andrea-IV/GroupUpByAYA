package com.andrea.groupup.Fragments

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
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
import com.andrea.groupup.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener

/**
 * A simple [Fragment] subclass.
 */

private const val TAG = "MAP"

class ChatMapFragment : Fragment(), OnMapReadyCallback, OnMyLocationButtonClickListener {
//class ChatMapFragment : SupportMapFragment() {
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private var mLocationPermissionGranted = false;

    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient

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
                println("ici 1")

                val location = this.mFusedLocationProviderClient.lastLocation
                location.addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful) {
                        Log.d(TAG, "onComplete: found location")
                        val current = it.result
                        println("move please")
                        if(current !== null)
                            moveCamera(LatLng(current.latitude, current.longitude), 15f);
                    } else {
                        Log.d(TAG, "onComplete: location not found")
                        Toast.makeText(this.activity, "Can't get your current location", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                println("ici 2")
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




    // -------------------------

    private fun moveCamera(latLng: LatLng, zoom: Float) {
        Log.d(TAG, "Moving camera to lat " + latLng.latitude + ", lng " + latLng.longitude);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
    }

    override fun onMapReady(mMap: GoogleMap) {
        Log.d(TAG, "onMapReady")
        this.mMap = mMap

        if(checkPermission()) {
            mLocationPermissionGranted = true
            getDeviceLocation();
            mMap.isMyLocationEnabled = true
            mMap.setOnMyLocationButtonClickListener(this)
        } else {
            requestPermissions()
        }

        //this.mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title("Current Location"))
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
