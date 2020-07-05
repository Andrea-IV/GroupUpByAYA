package com.andrea.groupup

import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Models.MeetingPoint
import com.andrea.groupup.Models.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class ShowMeetingPointActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_meeting_point)

        val mp = intent.getSerializableExtra("meetingpoint") as MeetingPoint
        val user = intent.getSerializableExtra("user") as User

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
    }

}