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
import com.andrea.groupup.Models.User

import com.andrea.groupup.R

/**
 * A simple [Fragment] subclass.
 */
class GroupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_group, container, false)
        val listItems = arrayListOf<User>()

        for (i in 0 until 5) {
            //listItems.add(User(i, "Mister ICU"))
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
