package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.andrea.groupup.Adapters.PagerViewAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.*
import com.android.volley.VolleyError
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import de.hdodenhof.circleimageview.CircleImageView
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

private const val TAG = "DETAIL"

class DetailsActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate{

    private lateinit var groupButton: ImageButton
    lateinit var chatMapButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var exploreButton: ImageButton
    private lateinit var backButton: ImageButton

    private lateinit var mViewPager: ViewPager
    private lateinit var mPagerAdapter: PagerViewAdapter

    lateinit var group: Group
    lateinit var user: User
    lateinit var token: String
//    var localplaces = ArrayList<LocalPlace>()
//    var travels = ArrayList<Travel>()
//    var meetingpoints: ArrayList<MeetingPoint> by Delegates.observable(ArrayList()) { property, oldValue, newValue ->
//        Log.d(TAG, "new value = $newValue")
//        Log.d(TAG, "old value = $oldValue")
//    }

    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    private lateinit var getGroupInfoHandler: Handler
    private val getGroupInfoRunnable =  object: Runnable {
        override fun run() {
            getGroup()
            getGroupInfoHandler.postDelayed(this, 5000)
        }
    }
    private lateinit var groupHttp: GroupHttp
//    lateinit var localplaceHttp: LocalPlaceHttp
//    lateinit var travelHttp: TravelHttp
//    lateinit var meetingPointHttp: MeetingPointHttp

    private fun getGroup() {
        groupHttp
            .getById(group.id, object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    val group = Mapper().mapper<JSONObject, Group>(jsonObject)
                    var isIn = false
                    group.members.forEach {
                        if(user.id == it.id) {
                            isIn = true
                        }
                    }

                    if(!isIn) {
                        finish()
                    }
                }

                override fun onError(error: VolleyError) {
                    Log.e(TAG, error.toString())
                }

            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        groupHttp = GroupHttp(this)
//        localplaceHttp = LocalPlaceHttp(this)
//        travelHttp = TravelHttp(this)
//        meetingPointHttp = MeetingPointHttp(this)


        group = intent.getSerializableExtra("Group") as Group
        user = intent.getSerializableExtra("User") as User
        user = group.members.filter { user.id == it.id }[0]
        token = intent.getStringExtra("Token")

        mViewPager = findViewById(R.id.mViewPager)

        groupButton = findViewById(R.id.groupButton)
        chatMapButton = findViewById(R.id.chatButton)
        calendarButton = findViewById(R.id.dateButton)
        exploreButton = findViewById(R.id.pinButton)
        backButton = findViewById(R.id.backButton)

        mPagerAdapter = PagerViewAdapter(supportFragmentManager)
        mViewPager.adapter = mPagerAdapter
        mViewPager.offscreenPageLimit = 5

        groupButton.setOnClickListener {
            mViewPager.currentItem = 0
        }

        chatMapButton.setOnClickListener {
            mViewPager.currentItem = 1
        }

        calendarButton.setOnClickListener {
            mViewPager.currentItem = 2
        }

        exploreButton.setOnClickListener {
            mViewPager.currentItem = 3
        }

        backButton.setOnClickListener {
            finish()
        }

        mViewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{

            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                changingTabs(position)
                val frag = mPagerAdapter.instantiateItem(mViewPager, position) as? FragmentInterface
                frag?.fragmentBecameVisible()
            }
        })

        mViewPager.currentItem = 0
        groupButton.setImageResource(R.drawable.ic_group_blue)

        getGroupInfoHandler = Handler(Looper.getMainLooper())
        getGroupInfoHandler.post(getGroupInfoRunnable)
    }

    private fun changingTabs(position: Int) {
        if(position == 0){
            groupButton.setImageResource(R.drawable.ic_group_blue)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 1){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_blue)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 2){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_blue)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_black)
        }
        if(position == 3){
            groupButton.setImageResource(R.drawable.ic_group_black)
            chatMapButton.setImageResource(R.drawable.ic_chat_black)
            calendarButton.setImageResource(R.drawable.ic_today_black)
            exploreButton.setImageResource(R.drawable.ic_pin_drop_blue)
        }
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
            /*UserHttp(this.requireActivity()).getByName(user.username, object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("USER", array.toString())
                    user.pp_link = (array[0] as JSONObject)["pp_link"].toString()
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "get photo - onError")
                    Log.e("USER", error.toString())
                }
            })*/
        }
    }

    override fun onCancelled() {
        TODO("Not yet implemented")
    }

    fun startUpload(uri: Uri){
        val uploadId = UUID.randomUUID().toString()
        uploadReceiver.setDelegate(this)
        uploadReceiver.setUploadID(uploadId)
        Log.d("UPLOAD", "HERE")

        MultipartUploadRequest(this, uploadId, Constants.BASE_URL + "/groups/picture")
            .addFileToUpload(getUriPath(uri), "picture") //Adding file
            .addHeader("Authorization", "Bearer $token")
            .addParameter("groupId", group.id.toString())
            .setMaxRetries(2)
            .startUpload()
    }


    private fun getUriPath(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

//    private fun loadAllGroupInfo() {
//        Log.d(TAG, "loadAllGroupInfo")
//        getMeetingPoints()
//    }
//
//    private fun getLocalPlaces(latlng: LatLng?) {
//
//    }
//
//    private fun getMeetingPoints() {
//        meetingPointHttp.getNow(group.id, object: VolleyCallbackArray {
//            override fun onResponse(array: JSONArray) {
//                Log.d(TAG, "getMeetingPointsNow - onResponse")
//                meetingpoints = Mapper().mapper(array)
//            }
//
//            override fun onError(error: VolleyError) {
//                Log.d(TAG, "getMeetingPointsNow - onError")
//                Log.e(TAG, error.toString())
//            }
//        })
//    }
}
