package com.andrea.groupup.Adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupWindow
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.andrea.groupup.Http.LocalPlaceHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.Tag
import com.andrea.groupup.R
import com.android.volley.VolleyError
import org.json.JSONObject

class TagAdapter(var items: ArrayList<Tag>, val localPlaceId: String, var ctx: Context, val layoutInflater: LayoutInflater) : RecyclerView.Adapter<TagAdapter.PlaceHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceHolder {
        return PlaceHolder(LayoutInflater.from(ctx).inflate(R.layout.list_of_tags, parent, false), localPlaceId, ctx, layoutInflater, this)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PlaceHolder, position: Int) {
        val item = items[position]
        holder.bindTags(item)
    }

    class PlaceHolder(val v: View, val id: String, private val ctx: Context, private val layoutInflater: LayoutInflater, val adapter: TagAdapter) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private var view: View = v
        private var tag: Tag? = null

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            val window = PopupWindow(v, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,true)
            val view = layoutInflater.inflate(R.layout.dialog_yes_no_maybe_i_don_t_know, null)
            view.findViewById<TextView>(R.id.textDialog).text = ctx.getString(R.string.ask_delete_place)
            window.contentView = view

            view.findViewById<Button>(R.id.yes).setOnClickListener {
                window.dismiss()
                removeItem(adapterPosition)
                if(id != "0"){
                    LocalPlaceHttp(ctx).deleteTag(adapter.items, id, object: VolleyCallback {
                        override fun onResponse(jsonObject: JSONObject) {
                            Log.d("DELETE PLACE", jsonObject.toString())
                        }

                        override fun onError(error: VolleyError): Unit {
                            Log.e("DELETE PLACE", "Delete place - onError")
                        }
                    })
                }
            }
            view.findViewById<Button>(R.id.no).setOnClickListener {
                window.dismiss()
            }
            view.findViewById<ConstraintLayout>(R.id.layout).setOnClickListener {
                window.dismiss()
            }
            window.showAtLocation(v, Gravity.CENTER, 0, 0)
        }

        private fun removeItem(position: Int) {
            adapter.items.removeAt(position)
            adapter.notifyDataSetChanged()
        }

        fun bindTags(item: Tag) {
            (view.findViewById<View>(R.id.name) as TextView).text = item.name
        }

    }
}