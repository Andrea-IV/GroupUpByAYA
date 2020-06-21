package com.andrea.groupup.Fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import com.andrea.groupup.Adapters.ParticipantAdapter
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User

import com.andrea.groupup.R

/**
 * A simple [Fragment] subclass.
 */
class GroupFragment : BaseFragment() {

    lateinit var group: Group

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_group, container, false)
        group = ACTIVITY.group

        val listItems = arrayListOf<User>()

        for (member in group.members) {
            listItems.add(member)
        }

        val adapter = ParticipantAdapter(listItems, requireContext())
        val listView: ListView = view.findViewById(R.id.listOfParticipants)
        listView.adapter = adapter

        /*listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(requireContext(), EventActivity::class.java)
                startActivity(intent)
            }
        }*/
        return view
    }

}
