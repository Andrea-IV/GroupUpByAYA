package com.andrea.groupup

import android.media.Image
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.MeetingPointHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.MeetingPoint
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.beust.klaxon.json
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_place.*
import org.json.JSONObject
import java.util.*

private const val TAG = "MEETING POINT"
class CreateMeetingPointActivity : AppCompatActivity() {

    private lateinit var spinner: Spinner
    private lateinit var saveButton: ImageView
    private lateinit var description: EditText
    private lateinit var coordXEdit: EditText
    private lateinit var coordYEdit: EditText

    private var meetingPoint: MeetingPoint? = null
    private lateinit var user: User
    private lateinit var token: String
    private lateinit var coords: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_meeting_point)
        saveButton = findViewById(R.id.create_meeting_point_save)
        description = findViewById(R.id.create_meeting_point_description)
        spinner = findViewById(R.id.create_meeting_point_spinner)
        coordXEdit = findViewById(R.id.create_meeting_point_coord_x)
        coordYEdit = findViewById(R.id.create_meeting_point_coord_y)

        coords = intent.getParcelableExtra("latlng")
        meetingPoint = intent.getSerializableExtra("meetingPoint") as MeetingPoint?
        user = intent.getSerializableExtra("user") as User
        token = intent.getStringExtra("token")

        findViewById<ImageView>(R.id.create_meeting_point_back).setOnClickListener {
            setResult(0)
            finish()
        }
        populateCoords()
        saveButton.setOnClickListener(save)
    }

    private fun populateCoords() {
        coordXEdit.setText(coords?.latitude.toString())
        coordYEdit.setText(coords?.longitude.toString())
    }

    val save = View.OnClickListener {
        val mp = MeetingPoint(
            coordinate_x = coordXEdit.text.toString(),
            coordinate_y = coordYEdit.text.toString(),
            description = description.text.toString(),
            UserId = user.id,
            GroupId = user.UserGroup.GroupId,
            expiration_date = getExpirationDate(spinner.selectedItem.toString())
        )

        createMeetingPoint(mp)
    }

    private fun getExpirationDate(t: String): Date {
        val time = Date()
        Log.d("TIME = ", time.time.toString())
        when (t) {
            "30min" -> {
                time.time = time.time + (30 * 60 * 1000)
            }
            "1h"    -> { time.time = time.time + (60 * 60 * 1000) }
            "1h30"  -> { time.time = time.time + (90 * 60 * 1000) }
            "2h"    -> { time.time = time.time + (120 * 60 * 1000) }
        }

        return time
    }

    private fun createMeetingPoint(meetingPoint: MeetingPoint) {
        Log.d(TAG, "createMeetingPoint")

        MeetingPointHttp(this).create(meetingPoint, token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d(TAG, "createMeetingPoint - onResponse")
                setResult(1)
                finish()
            }

            override fun onError(error: VolleyError) {
                Log.d(TAG, "createMeetingPoint - onError")
                Log.d(TAG, error.toString())
            }

        })
    }
}