package com.andrea.groupup.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Models.Tag
import com.andrea.groupup.R

class TagAdapter(var items: ArrayList<Tag>, var ctx: Context) : RecyclerView.Adapter<TagAdapter.PlaceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(LayoutInflater.from(ctx).inflate(R.layout.list_of_tags, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val item = items[position]
        holder.bindTags(item)
    }

    class PlaceHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var tag: Tag? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
        }

        fun bindTags(item: Tag) {
            (view.findViewById<View>(R.id.name) as TextView).text = item.name
        }

    }
}