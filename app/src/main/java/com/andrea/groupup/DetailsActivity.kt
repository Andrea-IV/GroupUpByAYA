package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager.widget.ViewPager
import com.andrea.groupup.Adapters.PagerViewAdapter
import com.andrea.groupup.Http.SingleUploadBroadcastReceiver
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import de.hdodenhof.circleimageview.CircleImageView
import net.gotev.uploadservice.MultipartUploadRequest
import java.net.URI
import java.util.*

class DetailsActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate{

    private lateinit var groupButton: ImageButton
    private lateinit var chatMapButton: ImageButton
    private lateinit var calendarButton: ImageButton
    private lateinit var exploreButton: ImageButton
    private lateinit var backButton: ImageButton

    private lateinit var mViewPager: ViewPager
    private lateinit var mPagerAdapter: PagerViewAdapter

    lateinit var group: Group
    lateinit var user: User
    lateinit var token: String
    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

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
            }
        })

        mViewPager.currentItem = 0
        groupButton.setImageResource(R.drawable.ic_group_blue)
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

}
