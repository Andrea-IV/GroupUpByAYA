package com.andrea.groupup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import com.andrea.groupup.Adapters.SelectedAdapter
import com.andrea.groupup.Models.Place

class EventActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        val listItems = arrayListOf<Place>()

        for (i in 0 until 10) {
            listItems.add(
                Place(i, "Gundam Statue", "Paris, France","Lorem ipsum dolor sit amet, \n" +
                        "consectetur adipiscing elit, sed do\n" +
                        "eiusmod tempor incididunt ut labore \n" +
                        "et dolore magna aliqua. Ut enim ad\n" +
                        "minim veniam, quis nostrud \n" +
                        "exercitation ullamco laboris nisi ut \n" +
                        "aliquip ex ea commodo consequat. \n" +
                        "Duis aute irure dolor in reprehenderit\n" +
                        "in voluptate velit esse cillum dolore\n" +
                        " eu fugiat nulla pariatur.", 4.5,i)
            )
        }

        val adapter = SelectedAdapter(listItems, this)
        val listView: ListView = findViewById(R.id.listOfSelected)
        findViewById<TextView>(R.id.date).text = "19 Mai"
        listView.adapter = adapter

        findViewById<ImageButton>(R.id.back).setOnClickListener {
            finish()
        }
    }
}
