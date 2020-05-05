package com.andrea.groupup.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.andrea.groupup.Fragments.*

internal class PagerViewAdapter (fm:FragmentManager?) : FragmentPagerAdapter(fm){
    override fun getItem(position: Int): Fragment? {
        return when (position){
            0 -> {
                GroupFragment()
            }
            1 -> {
                ChatMapFragment()
            }
            2 -> {
                CalendarFragment()
            }
            3 -> {
                ExploreFragment()
            }
            4 -> {
                SettingsFragment()
            }
            else -> GroupFragment()
        }
    }

    override fun getCount(): Int {
        return 5
    }

}