package com.andrea.groupup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.UserHttp
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError

import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener{
            val intent = Intent(this, GroupActivity::class.java)
            UserHttp(this).getByName("bunuu54", object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("USER", "getUser - onResponse")
                    Log.d("USER", array[0].toString())
                    intent.putExtra("User",array[0].toString())
                    startActivity(intent)
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "getUser - onError")
                    Log.e("USER", error.javaClass.toString())
                }
            })
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener{
            val intent = Intent(this, GroupActivity::class.java)
            UserHttp(this).getByName("bunuu", object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("USER", "getUser - onResponse")
                    Log.d("USER", array[0].toString())
                    intent.putExtra("User",array[0].toString())
                    startActivity(intent)
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "getUser - onError")
                    Log.e("USER", error.javaClass.toString())
                }
            })
        }
    }
}
