package com.andrea.groupup.Fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.andrea.groupup.Adapters.PlaceAdapter
import com.andrea.groupup.Models.Place
import com.andrea.groupup.R


/**
 * A simple [Fragment] subclass.
 */
class ExploreFragment : Fragment() {
    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_explore, container, false)
        val listItems = arrayListOf<Place>()

        for (i in 0 until 10) {
            listItems.add(
                Place(i, "Gundam Statue", "Lorem ipsum dolor sit amet, \n" +
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

        adapter = PlaceAdapter(listItems, requireContext())
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        layoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_NONE
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter;
        return view
    }

}
