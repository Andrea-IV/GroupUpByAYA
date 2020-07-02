package com.andrea.groupup.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.andrea.groupup.Http.FriendHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.User
import com.andrea.groupup.R
import com.android.volley.VolleyError
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class FriendAdapter(items: ArrayList<User>, val user: User, ctx: Context) :
    ArrayAdapter<User>(ctx,
        R.layout.list_of_participants, items) {

    var arrayList = items
    var tempList = ArrayList(arrayList)

    //view holder is used to prevent findViewById calls
    private class ParticipantViewHolder {
        internal var image: ImageView? = null
        internal var name: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: ParticipantViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_participants, viewGroup, false)

            viewHolder = ParticipantViewHolder()
            viewHolder.image = view!!.findViewById<View>(R.id.profile_image) as ImageView
            viewHolder.name = view.findViewById<View>(R.id.name) as TextView
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as ParticipantViewHolder
        }

        val participant = getItem(i)
        //viewHolder.image!!.src = event!!.date
        viewHolder.name!!.text = participant.username

        view.findViewById<ImageView>(R.id.imageView).setOnClickListener {
            val popupMenu = PopupMenu(context, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.leave -> {
                        deleteFriendMenu(participant, i)
                        true
                    }
                    else -> false
                }
            }

            popupMenu.inflate(R.menu.menu_participant)



            try {
                val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
                fieldMPopup.isAccessible = true
                val mPopup = fieldMPopup.get(popupMenu)
                mPopup.javaClass
                    .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                    .invoke(mPopup, true)
            } catch (e: Exception){
                Log.e("Main", "Error showing menu icons.", e)
            } finally {
                popupMenu.show()
            }
        }

        view.tag = viewHolder

        return view
    }

    private fun deleteFriendMenu(participant: User, i: Int){
        FriendHttp(context).removeFriend(user.id.toString(), participant.id.toString(), object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("LEAVE", jsonObject.toString())
                arrayList.removeAt(i)
                notifyDataSetChanged()
            }

            override fun onError(error: VolleyError) {
                Log.e("LEAVE", "leave group - onError")
                arrayList.removeAt(i)
                notifyDataSetChanged()
            }
        })
    }
}