package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Adapters.TagAdapter
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.SingleUploadBroadcastReceiver
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.LocalPlace
import com.android.volley.VolleyError
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONObject
import java.util.*

class PlaceActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate {
    private var actualPhotoIndex: Int = 0
    private val REQUEST_CODE = 100
    private lateinit var localPlace: LocalPlace
    private lateinit var token: String
    private var from: String? = null

    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false

    private var uri: Uri? = null
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        token = intent.getStringExtra("TOKEN")
        localPlace = intent.getSerializableExtra("PLACE") as LocalPlace
        from = intent.getStringExtra("FROM")

        if("map".equals(from)) {
            findViewById<Button>(R.id.button2).visibility = View.GONE
        }

        if(!localPlace.Photos.isNullOrEmpty()){
            Picasso.get().load(Constants.BASE_URL + "/" + localPlace.Photos[actualPhotoIndex].link).into(findViewById<ImageView>(R.id.imageView2))
            findViewById<ImageView>(R.id.next).setOnClickListener {
                changePhoto(true)
            }

            findViewById<ImageView>(R.id.previous).setOnClickListener {
                changePhoto(false)
            }
        }

        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        val adapter = TagAdapter(localPlace.Tags, this)
        val recyclerView: RecyclerView = findViewById(R.id.listOfTags)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.title).text = localPlace.name
        if(localPlace.translations.isNotEmpty()){
            findViewById<TextView>(R.id.description).text = localPlace.translations[0].content
        }

        findViewById<TextView>(R.id.rating).text = localPlace.Ratings.toString() + " /5"
        findViewById<TextView>(R.id.distance).text = localPlace.distance.toString() + " km"

        findViewById<TextView>(R.id.descriptionTitle).setOnClickListener {
            focusDescription()
        }

        findViewById<TextView>(R.id.tagsTitle).setOnClickListener {
            focusTags()
        }

        findViewById<ImageView>(R.id.add).setOnClickListener {
            initMenu(it)
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }
    }

    private fun focusTags(){
        findViewById<TextView>(R.id.tagsTitle).setTextColor(ContextCompat.getColor(this, R.color.selectedShadedText))
        findViewById<TextView>(R.id.descriptionTitle).setTextColor(ContextCompat.getColor(this, R.color.shadedText))
        val cl = findViewById<ConstraintLayout>(R.id.activity_constraint)
        val cs = ConstraintSet()
        cs.clone(cl)
        cs.setHorizontalBias(R.id.selected, 0.39f)
        cs.applyTo(cl)
        findViewById<TextView>(R.id.description).visibility = View.GONE
        findViewById<RecyclerView>(R.id.listOfTags).visibility = View.VISIBLE
    }

    private fun focusDescription(){
        findViewById<TextView>(R.id.descriptionTitle).setTextColor(ContextCompat.getColor(this, R.color.selectedShadedText))
        findViewById<TextView>(R.id.tagsTitle).setTextColor(ContextCompat.getColor(this, R.color.shadedText))
        val cl = findViewById<ConstraintLayout>(R.id.activity_constraint)
        val cs = ConstraintSet()
        cs.clone(cl)
        cs.setHorizontalBias(R.id.selected, 0.18f)
        cs.applyTo(cl)
        findViewById<TextView>(R.id.description).text = localPlace.translations[0].content
        findViewById<TextView>(R.id.description).visibility = View.VISIBLE
        findViewById<RecyclerView>(R.id.listOfTags).visibility = View.GONE
    }

    private fun changePhoto(isNext: Boolean){
        if(isNext){
            actualPhotoIndex++
            if(actualPhotoIndex >= localPlace.Photos.size){
                actualPhotoIndex = 0;
            }
        }else{
            actualPhotoIndex--
            if(actualPhotoIndex < 0){
                actualPhotoIndex = localPlace.Photos.size - 1;
            }
        }

        Picasso.get().load(Constants.BASE_URL + "/" + localPlace.Photos[actualPhotoIndex].link).into(findViewById<ImageView>(R.id.imageView2))
    }

    private fun initMenu(it: View){
        val popupMenu = PopupMenu(this, it)
        popupMenu.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.photo -> {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, REQUEST_CODE)
                    true
                }
                R.id.tag -> {
                    true
                }
                else -> false
            }
        }

        popupMenu.inflate(R.menu.menu_place)

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            uri = data?.data

            if(checkPermission()) {
                storagePermissionGranted = true
                startUpload()
            } else {
                requestPermissions()
            }

            findViewById<ImageView>(R.id.imageView2).setImageURI(data?.data) // handle chosen image
        }
    }

    private fun startUpload(){
        val uploadId = UUID.randomUUID().toString()
        uploadReceiver.setDelegate(this)
        uploadReceiver.setUploadID(uploadId)

        MultipartUploadRequest(this, uploadId, Constants.BASE_URL + "/photos")
            .addFileToUpload(getUriPath(), "images") //Adding file
            .addParameter("localPlaceId", localPlace.id.toString())
            .addHeader("Authorization", "Bearer $token")
            .setMaxRetries(2)
            .startUpload()
    }


    private fun getUriPath(): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }


    override fun onResume() {
        super.onResume()
        uploadReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        uploadReceiver.unregister(this)
    }

    override fun onProgress(progress: Int) {
        //your implementation
    }

    override fun onProgress(uploadedBytes: Long, totalBytes: Long) {
        //your implementation
    }

    override fun onError(exception: java.lang.Exception?) {
        Log.e("ERROR", exception.toString())
    }

    override fun onCompleted(serverResponseCode: Int, serverResponseBody: ByteArray?) {
        if(serverResponseCode.toString() == "201"){
            LocalPlaceHttp(this).getOne(localPlace.id.toString(),  object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    val gson: Gson = Gson()
                    localPlace.Photos = (gson.fromJson(jsonObject.toString(), LocalPlace::class.java)).Photos
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.toString())
                }
            })
        }
    }

    override fun onCancelled() {
        TODO("Not yet implemented")
    }

    private fun checkPermission() : Boolean {
        Log.d("PERMISSION", "checkPermission")
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        Log.d("PERMISSION", "requestPermissions")
        ActivityCompat.requestPermissions(this, permissions,1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("PERMISSION", "onRequestPermissionsResult")
        if(requestCode == 1) {
            if(grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if(it != PackageManager.PERMISSION_GRANTED) {
                        storagePermissionGranted = false
                        return
                    }
                }
                storagePermissionGranted = true;
                startUpload()
            }
        }
    }
}
