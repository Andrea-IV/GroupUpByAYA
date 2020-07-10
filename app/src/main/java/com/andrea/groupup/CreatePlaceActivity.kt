package com.andrea.groupup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.LocaleList
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.*
import com.android.volley.VolleyError
import com.google.android.gms.maps.model.LatLng
import org.json.JSONObject
import java.util.*

class CreatePlaceActivity : AppCompatActivity() {
    private lateinit var token: String
    private lateinit var user: User

    private lateinit var place: LocalPlace
    private lateinit var title: EditText
    private lateinit var address: EditText
    private lateinit var description: EditText
    private lateinit var rating: EditText
    private lateinit var location: LatLng

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_place)

        user = intent.getSerializableExtra("USER") as User
        token = intent.getStringExtra("TOKEN")
        location = intent.getParcelableExtra("location") as LatLng

        title = findViewById(R.id.title)
        address = findViewById(R.id.address)
        description = findViewById(R.id.description)
        rating = findViewById(R.id.rating)

        title.hint = getString(R.string.title_hint)
        address.hint = getString(R.string.adress_hint)
        description.hint = getString(R.string.description_hint)
        rating.hint = getString(R.string.rating_hint)

        findViewById<Button>(R.id.createPlace).setOnClickListener{
            createPlace()
        }
    }

    private fun createPlace(){
        if(title.text.isNotEmpty() && address.text.isNotEmpty() && description.text.isNotEmpty() && rating.text.isNotEmpty()){
            place = LocalPlace(0, title.text.toString(), location.latitude.toString(), location.longitude.toString(), address.text.toString(), "0", "0", Date(), user.id, ArrayList<Photo>(), rating.text.toString().toDouble(), ArrayList<Translation>(), ArrayList<Tag>(), 0.0, rating.text.toString().toInt(), 0,  null)
            LocalPlaceHttp(this).createPlace(place, token,  object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    finish()
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.javaClass.toString())
                }
            })
        }
    }
}
