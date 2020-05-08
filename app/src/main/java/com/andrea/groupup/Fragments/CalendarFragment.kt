package com.andrea.groupup.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.andrea.groupup.Adapters.EventAdapter
import com.andrea.groupup.Models.Event

import com.andrea.groupup.R

/**
 * A simple [Fragment] subclass.
 */
class CalendarFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_calendar, container, false)
        val listItems = arrayListOf<Event>()

        for (i in 0 until 10) {
            listItems.add(Event(i, ((i + 14).toString() + " Mai"), "Paris, France"))
        }

        val adapter = EventAdapter(listItems, requireContext())
        val listView: ListView = view.findViewById(R.id.listOfEvents)
        listView.adapter = adapter
        return view
    }

}
