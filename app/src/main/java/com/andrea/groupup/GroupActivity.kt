package com.andrea.groupup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView

class GroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val listItems = arrayListOf<Group>()

        for (i in 0 until 5) {
            listItems.add(Group(i, "The " + i + " Group", 0))
        }

        val adapter = GroupAdapter(listItems, this)
        val listView:ListView = findViewById(R.id.listOfGroups)
        listView.adapter = adapter

        listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(this@GroupActivity, MapsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
