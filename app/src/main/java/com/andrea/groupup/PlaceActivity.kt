package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Adapters.TagAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.*
import com.android.volley.VolleyError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class PlaceActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate {
    private var actualPhotoIndex: Int = 0
    private val REQUEST_CODE = 100
    private lateinit var localPlace: LocalPlace
    private lateinit var group: Group
    private lateinit var user: User
    private lateinit var token: String
    private lateinit var adapter: TagAdapter

    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false

    private var uri: Uri? = null
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        token = intent.getStringExtra("TOKEN")
        localPlace = intent.getSerializableExtra("PLACE") as LocalPlace
        group = intent.getSerializableExtra("GROUP") as Group
        user = intent.getSerializableExtra("USER") as User

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
        adapter = TagAdapter(localPlace.Tags, this)
        val recyclerView: RecyclerView = findViewById(R.id.listOfTags)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        findViewById<TextView>(R.id.title).text = localPlace.name
        if(localPlace.translations.isNotEmpty()){
            findViewById<TextView>(R.id.description).text = localPlace.translations[0].content
        }

        if(localPlace.Ratings.toString() == "null"){
            findViewById<TextView>(R.id.rating).text = resources.getText(R.string.no_rating)
        }else{
            findViewById<TextView>(R.id.rating).text = localPlace.Ratings.toString() + " /5"
        }

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

        findViewById<ImageView>(R.id.imageView3).setOnClickListener {
            displayAddRating()
        }

        findViewById<ImageView>(R.id.back).setOnClickListener {
            setResult(0)
            finish()
        }
    }

    private fun displayAddRating(){
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        dialog.setContentView(view)

        when (localPlace.UserRating) {
            1 -> view.findViewById<RadioButton>(R.id.one_rating).isChecked = true
            2 -> view.findViewById<RadioButton>(R.id.two_rating).isChecked = true
            3 -> view.findViewById<RadioButton>(R.id.three_rating).isChecked = true
            4 -> view.findViewById<RadioButton>(R.id.four_rating).isChecked = true
            5 -> view.findViewById<RadioButton>(R.id.five_rating).isChecked = true
            else -> {
                view.findViewById<RadioButton>(R.id.no_rating).isChecked = true
            }
        }

        view.findViewById<Button>(R.id.AddRating).setOnClickListener {
            ratingAction(view)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun ratingAction(it: View){
        val idSelected = it.findViewById<RadioGroup>(R.id.RatingValue).checkedRadioButtonId
        val selectedString = resources.getResourceEntryName(idSelected)
        val resID = resources.getIdentifier(selectedString, "id", packageName)
        val valueSelected = it.findViewById<RadioButton>(resID).text.toString()
        if(localPlace.UserRating.toString() == "null"){
            if(valueSelected != "No rating"){
                createRating(valueSelected.toInt())
            }
        }else{
            if(valueSelected == "No rating"){
                deleteRating()
            }else if(valueSelected.toInt() != localPlace.UserRating){
                modifyRating(valueSelected.toInt())
            }
        }
    }

    private fun createTag(value: String){
        LocalPlaceHttp(this).addTag(localPlace.Tags, value, localPlace.id.toString(), object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("TAG", "add Tag - OK")
                val lpRes = Mapper().mapper<JSONArray, List<Tag>>(jsonObject["Tags"] as JSONArray)
                Log.d("TAG", lpRes.toString())
                localPlace.Tags.clear()
                for(tag in lpRes) {
                    localPlace.Tags.add(tag)
                }

                adapter.notifyDataSetChanged()
            }

            override fun onError(error: VolleyError) {
                Log.e("TAG", "add Tag - onError")
                Log.e("TAG", error.toString())
            }
        })

    }

    private fun createRating(rating: Int){
        RatingHttp(this).createRating(rating.toString(),localPlace.id.toString(), token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("RATING", "create rating - OK")
                Log.d("RATING", jsonObject.toString())
            }

            override fun onError(error: VolleyError) {
                Log.e("RATING", "create rating - onError")
                Log.e("RATING", error.toString())
            }
        })
        localPlace.UserRating = rating
    }

    private fun modifyRating(rating: Int){
        RatingHttp(this).modifyRating(rating.toString(),localPlace.id.toString(), token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("RATING", "modify rating - OK")
                Log.d("RATING", jsonObject.toString())
            }

            override fun onError(error: VolleyError) {
                Log.e("RATING", "modify rating - onError")
                Log.e("RATING", error.toString())
            }
        })
        localPlace.UserRating = rating
    }

    private fun deleteRating(){
        RatingHttp(this).deleteRating(localPlace.id.toString(), token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("RATING", "delete rating - OK")
                Log.d("RATING", jsonObject.toString())
            }

            override fun onError(error: VolleyError) {
                Log.e("RATING", "delete rating - onError")
                Log.e("RATING", error.toString())
            }
        })
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
                R.id.event -> {
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.dialog_create_plan, null)
                    dialog.setContentView(view)

                    val datePicker: DatePicker = view.findViewById(R.id.date)
                    datePicker.minDate = System.currentTimeMillis() - 1;

                    view.findViewById<Button>(R.id.create).setOnClickListener{
                        searchEvent(getDate(datePicker))
                        dialog.dismiss()
                    }

                    dialog.show()
                    true
                }
                R.id.tag -> {
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.dialog_tag, null)
                    dialog.setContentView(view)

                    view.findViewById<Button>(R.id.AddTag).setOnClickListener{
                        val tagValue = view.findViewById<EditText>(R.id.TagValue).text.toString()
                        if(tagValue.isNotEmpty() && tagValue.isNotBlank()){
                            createTag(tagValue)
                        }
                        dialog.dismiss()
                    }

                    dialog.show()
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

    private fun searchEvent(date: String){
        EventHttp(this).getEvents(group.id.toString(), object: VolleyCallbackArray {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(array: JSONArray) {
                var lpRes = Mapper().mapper<JSONArray, List<Event>>(array)
                for(i: Int in 0 until array.length()){
                    val item =  array.getJSONObject(i)
                    for (j: Int in 0 until (item["LocalPlaces"] as JSONArray).length()){
                        lpRes[i].LocalPlaces[j].pos = (((item["LocalPlaces"] as JSONArray)[j] as JSONObject)["TravelLocalplace"]as JSONObject)["position"] as Int
                    }
                }
                selectTypeOfCreation(lpRes, date)
            }

            override fun onError(error: VolleyError): Unit {
                Log.e("EVENTS", "Event - onError")
            }
        })
    }

    private fun getDate(datePicker: DatePicker): String{
        var day: String = datePicker.dayOfMonth.toString()
        var month: String = (datePicker.month + 1).toString()
        var year: String = datePicker.year.toString()

        if(month.toInt() <= 9){
            month = "0$month"
        }
        if(day.toInt() <= 9){
            day = "0$day"
        }

        return "$year-$month-$day"
    }

    private fun selectTypeOfCreation(lpRes: List<Event>, date: String){
        var found = false

        for(event in lpRes) {
            event.travel_date_original = event.travel_date
            event.travel_date = event.travel_date.substring(0, event.travel_date.indexOf("T"))
            if(event.UserId == user.id && event.travel_date == date){
                Log.d("Event", event.toString())
                modifyEvent(event)
                found = true
                break
            }
        }

        if(!found){
            createEvent(date)
        }
    }

    private fun modifyEvent(event: Event){
        EventHttp(this).modifyEvents(event, localPlace.id.toString(), token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("EVENT", jsonObject.toString())
            }

            override fun onError(error: VolleyError): Unit {
                Log.e("EVENTS", "Event - onError")
            }
        })
    }

    private fun createEvent(date: String){
        EventHttp(this).createEvents(date, group.id.toString(), localPlace.id.toString(), token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("EVENT", jsonObject.toString())
            }

            override fun onError(error: VolleyError): Unit {
                Log.e("EVENTS", "Event - onError")
            }
        })
    }
}
