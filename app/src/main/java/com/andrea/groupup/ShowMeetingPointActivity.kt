package com.andrea.groupup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Http.MeetingPointHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.MeetingPoint
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

private const val TAG = "SHOW MEETING POINT"
class ShowMeetingPointActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_meeting_point)

        val mp = intent.getSerializableExtra("meetingpoint") as MeetingPoint
        val user = intent.getSerializableExtra("user") as User
        val creator = intent.getBooleanExtra("creator", false)
        val token = intent.getStringExtra("token")

        findViewById<TextView>(R.id.show_meeting_point_username).text = user.username
        Picasso.get().load(Constants.BASE_URL + "/" + user.pp_link).into(findViewById<CircleImageView>(R.id.show_meeting_point_picture))
        var desc = "Pas de description"
        if(!mp.description.equals("")) {
            desc = mp.description
        }
        findViewById<TextView>(R.id.show_meeting_point_description).text = desc

        findViewById<ImageView>(R.id.show_meeting_point_back).setOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.meetingPointDirection).setOnClickListener {
            val data = Intent()
            data.putExtra("location", LatLng(mp.coordinate_x.toDouble(), mp.coordinate_y.toDouble()))
            setResult(1, data)
            finish()
        }

        if(creator) {
            val delete = findViewById<TextView>(R.id.show_meeting_point_delete)
            delete.visibility = View.VISIBLE
            delete.setOnClickListener {

                MeetingPointHttp(this).delete(mp.id!!, token, object : VolleyCallback {
                    override fun onResponse(jsonObject: JSONObject) {
                        Log.d(TAG, "SHOW MEETING POINT - delete - onResponse")
                        finish()
                    }

                    override fun onError(error: VolleyError) {
                        Log.d(TAG, "SHOW MEETING POINT - delete - onError")
                        println(error.toString())
                    }
                })
            }
        }
    }

}