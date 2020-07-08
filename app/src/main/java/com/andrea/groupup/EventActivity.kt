package com.andrea.groupup


import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.andrea.groupup.Adapters.SelectedAdapter
import com.andrea.groupup.Models.EventDisplay
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EventActivity : AppCompatActivity() {

    private lateinit var token: String
    private lateinit var user: User
    private lateinit var group: Group
    private lateinit var eventDisplay: EventDisplay

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        token = intent.getStringExtra("TOKEN")
        group = intent.getSerializableExtra("GROUP") as Group
        user = intent.getSerializableExtra("USER") as User
        eventDisplay = intent.getSerializableExtra("EVENTDISPLAY") as EventDisplay

        val adapter = SelectedAdapter(eventDisplay.users, user, group, token, eventDisplay.events,this)
        val listView: ListView = findViewById(R.id.listOfSelected)
        listView.adapter = adapter

        Log.d("PLACE2", eventDisplay.events.toString())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(eventDisplay.date, formatter)
        val finalFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")
        findViewById<TextView>(R.id.date).text = date.format(finalFormatter).toString()

        findViewById<ImageButton>(R.id.back).setOnClickListener {
            finish()
        }
    }
}
