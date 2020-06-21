package com.andrea.groupup.Fragments

import android.content.Context
import androidx.fragment.app.Fragment
import com.andrea.groupup.DetailsActivity

abstract class BaseFragment : Fragment() {
    lateinit var ACTIVITY: DetailsActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        ACTIVITY = context as DetailsActivity
    }
}