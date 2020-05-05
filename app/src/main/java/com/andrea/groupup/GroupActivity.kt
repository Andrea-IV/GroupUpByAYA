package com.andrea.groupup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import com.andrea.groupup.Adapters.GroupAdapter
import com.andrea.groupup.Models.Group

class GroupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val listItems = arrayListOf<Group>()

        for (i in 0 until 5) {
            listItems.add(
                Group(i, "The " + i + " Group", 3, 0)
            )
        }

        val adapter = GroupAdapter(listItems, this)
        val gridView: GridView = findViewById(R.id.listOfGroups)
        gridView.adapter = adapter

        gridView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(this@GroupActivity, DetailsActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
