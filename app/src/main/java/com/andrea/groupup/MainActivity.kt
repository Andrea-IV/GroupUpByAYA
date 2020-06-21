package com.andrea.groupup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button
import com.andrea.groupup.Http.UserHttp
import com.andrea.groupup.Http.VolleyCallback
import com.android.volley.VolleyError
import org.json.JSONObject
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.button)
        button.setOnClickListener{
            val intent = Intent(this, GroupActivity::class.java)
            UserHttp(this).login("bunuu54", "test", object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d("USER", "getUser - onResponse")
                    /*val fileOutputStream: FileOutputStream
                    try {
                        fileOutputStream = openFileOutput("token", Context.MODE_PRIVATE)
                        fileOutputStream.write(jsonObject.get("token").toString().substring().toByteArray())
                    }catch (e: Exception){
                        e.printStackTrace()
                    }*/
                    intent.putExtra("User", jsonObject.toString())
                    startActivity(intent)
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.toString())
                }
            })
        }

        val button2 = findViewById<Button>(R.id.button2)
        button2.setOnClickListener{
            val intent = Intent(this, GroupActivity::class.java)
            UserHttp(this).login("bunuu", "test", object: VolleyCallback {
                override fun onResponse(jsonObject: JSONObject) {
                    Log.d("USER", "getUser - onResponse")
                    Log.d("USER", jsonObject.toString())
                    intent.putExtra("User", jsonObject.toString())
                    startActivity(intent)
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.javaClass.toString())
                }
            })
        }
    }
}
