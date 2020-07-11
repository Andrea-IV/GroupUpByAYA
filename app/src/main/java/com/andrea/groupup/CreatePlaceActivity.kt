package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
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
import kotlinx.android.synthetic.main.activity_group.*
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList

class CreatePlaceActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate {
    private lateinit var token: String
    private lateinit var user: User
    private lateinit var group: Group
    private var actualPhotoIndex: Int = 0
    private val REQUEST_CODE = 100
    private var counter: Int = 0
    private lateinit var place: LocalPlace
    private lateinit var title: EditText
    private lateinit var description: EditText
    private lateinit var rating: TextView
    private lateinit var adapter: TagAdapter
    private var dataPhoto = ArrayList<Uri>()
    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false
    private val uploadReceiver = SingleUploadBroadcastReceiver()
    private lateinit var location: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_place)

        user = intent.getSerializableExtra("USER") as User
        group = intent.getSerializableExtra("GROUP") as Group
        token = intent.getStringExtra("TOKEN")
        location = intent.getParcelableExtra("location") as LatLng

        place = LocalPlace(0, "", "", "", "",  "", Date(), user.id, group.id, ArrayList<Photo>(), 0.0, ArrayList<Tag>(), 0.0, 0, 0, null)
        place.id = 0

        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        rating = findViewById(R.id.rating)

        title.hint = getString(R.string.title_hint)
        description.hint = getString(R.string.description_hint)
        rating.hint = getString(R.string.rating_hint)

        var layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        adapter = TagAdapter(place.Tags, 0.toString(),this, layoutInflater)
        val recyclerView: RecyclerView = findViewById(R.id.listOfTags)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        findViewById<ImageView>(R.id.next).setOnClickListener {
            changePhoto(true)
        }

        findViewById<ImageView>(R.id.previous).setOnClickListener {
            changePhoto(false)
        }

        findViewById<Button>(R.id.createPlace).setOnClickListener{
            createPlace()
        }

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
            finish()
        }
    }

    private fun displayAddRating(){
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        dialog.setContentView(view)

        when (place.UserRating) {
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
        if(valueSelected != "No rating"){
            place.UserRating = valueSelected.toInt()
            rating.text = valueSelected
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
        findViewById<TextView>(R.id.description).visibility = View.VISIBLE
        findViewById<RecyclerView>(R.id.listOfTags).visibility = View.GONE
    }

    private fun changePhoto(isNext: Boolean){
        if(dataPhoto.size != 0){
            if(isNext){
                actualPhotoIndex++
                if(actualPhotoIndex >= dataPhoto.size){
                    actualPhotoIndex = 0;
                }
            }else{
                actualPhotoIndex--
                if(actualPhotoIndex < 0){
                    actualPhotoIndex = dataPhoto.size - 1;
                }
            }
            findViewById<ImageView>(R.id.imageView2).setImageURI(dataPhoto[actualPhotoIndex])
        }
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
                    val dialog = BottomSheetDialog(this)
                    val view = layoutInflater.inflate(R.layout.dialog_tag, null)
                    dialog.setContentView(view)

                    view.findViewById<Button>(R.id.AddTag).setOnClickListener{
                        val tagValue = view.findViewById<EditText>(R.id.TagValue).text.toString()
                        if(tagValue.isNotEmpty() && tagValue.isNotBlank()){
                            place.Tags.add(Tag(0, tagValue))
                            adapter.notifyDataSetChanged()
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
        popupMenu.menu.findItem(R.id.event).isVisible = false
        popupMenu.menu.findItem(R.id.deletePlace).isVisible = false

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
            data?.data?.let { dataPhoto.add(it) }
            findViewById<ImageView>(R.id.imageView2).setImageURI(data?.data) // handle chosen image
        }
    }

    private fun startUpload(){
        for(photo in dataPhoto){
            var uploadId = UUID.randomUUID().toString()
            uploadReceiver.setDelegate(this)
            uploadReceiver.setUploadID(uploadId)

            MultipartUploadRequest(this, uploadId, Constants.BASE_URL + "/photos")
                .addFileToUpload(getUriPath(photo), "images") //Adding file
                .addParameter("localPlaceId", place.id.toString())
                .addHeader("Authorization", "Bearer $token")
                .setMaxRetries(2)
                .startUpload()
        }
    }


    private fun getUriPath(uri: Uri): String? {
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
            counter++
            if(counter == dataPhoto.size){
                finish()
            }
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

    private fun createPlace(){
        if(title.text.isNotEmpty() && description.text.isNotEmpty() && rating.text.isNotEmpty()){
            val params = "{\"name\":\"${title.text}\", \"coordinate_x\":\"${place.coordinate_x}\", \"coordinate_y\":\"${place.coordinate_y}\", \"address\":\"\", \"description\":\"${description.text}\", \"groupId\":\"${place.GroupId}\"}"
            LocalPlaceHttp(this).createPlace(params, token,  object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    val gson = Gson()
                    val tempTags = place.Tags
                    place = gson.fromJson(jsonObject.toString(), LocalPlace::class.java)
                    place.Tags = tempTags
                    addTags(tempTags)
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.javaClass.toString())
                }
            })
        }
    }

    private fun addTags(tags: ArrayList<Tag>){
        LocalPlaceHttp(this).addTags(tags, place.id.toString(),  object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                addRating()
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.javaClass.toString())
            }
        })
    }

    private fun addRating(){
        RatingHttp(this).createRating(rating.text.toString(), place.id.toString(), token,  object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                if(checkPermission()) {
                    storagePermissionGranted = true
                    startUpload()
                } else {
                    requestPermissions()
                }
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.javaClass.toString())
            }
        })
    }
}
