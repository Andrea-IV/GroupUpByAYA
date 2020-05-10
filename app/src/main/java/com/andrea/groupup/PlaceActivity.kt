package com.andrea.groupup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.andrea.groupup.Models.Place

class PlaceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)

        val place = Place(1, "Paris, France","Gundam Statue", "Lorem ipsum dolor sit amet, \n" +
                "consectetur adipiscing elit, sed do\n" +
                "eiusmod tempor incididunt ut labore \n" +
                "et dolore magna aliqua. Ut enim ad\n" +
                "minim veniam, quis nostrud \n" +
                "exercitation ullamco laboris nisi ut \n" +
                "aliquip ex ea commodo consequat. \n" +
                "Duis aute irure dolor in reprehenderit\n" +
                "in voluptate velit esse cillum dolore\n" +
                " eu fugiat nulla pariatur.", 4.5,1)

        findViewById<TextView>(R.id.title).text = place.title
        findViewById<TextView>(R.id.description).text = place.description
        findViewById<TextView>(R.id.rating).text = place.rating.toString()

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }
    }
}
