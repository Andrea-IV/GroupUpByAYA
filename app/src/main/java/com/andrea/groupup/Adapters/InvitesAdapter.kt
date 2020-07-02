package com.andrea.groupup.Adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrea.groupup.Http.FriendHttp
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.User
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject

class InvitesAdapter(items: ArrayList<User>, val user: User, val token: String, val noneTextView: TextView, ctx: Context) :
    ArrayAdapter<User>(ctx,
        R.layout.list_of_friends_invites, items) {

    var arrayList = items

    //view holder is used to prevent findViewById calls
    private class ParticipantViewHolder {
        internal var image: ImageView? = null
        internal var name: TextView? = null
        internal var accept: Button? = null
        internal var refuse: Button? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: ParticipantViewHolder

        if (view == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_of_friends_invites, viewGroup, false)

            viewHolder = ParticipantViewHolder()
            viewHolder.image = view!!.findViewById<View>(R.id.profile_image) as ImageView
            viewHolder.name = view.findViewById<View>(R.id.name) as TextView
            viewHolder.accept = view.findViewById<View>(R.id.accept) as Button
            viewHolder.refuse = view.findViewById<View>(R.id.refuse) as Button
        } else {
            //no need to call findViewById, can use existing ones from saved view holder
            viewHolder = view.tag as ParticipantViewHolder
        }

        val friend = getItem(i)
        //viewHolder.image!!.src = event!!.date
        viewHolder.name!!.text = friend.username
        viewHolder.accept!!.setOnClickListener {
            acceptInvite(friend)
        }
        viewHolder.refuse!!.setOnClickListener {
            refuseInvite(friend)
        }

        view.tag = viewHolder

        return view
    }

    private fun acceptInvite(friend: User){
        FriendHttp(context).acceptRequest(friend.id.toString(), token, object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("INVITES", array.toString())
                arrayList.remove(friend)

                if(arrayList.isEmpty()){
                    noneTextView.visibility = View.VISIBLE
                }

                notifyDataSetChanged()

            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "FriendsList - onError")
                Log.e("USER", error.toString())
            }
        })
    }

    private fun refuseInvite(friend: User){
        FriendHttp(context).removeFriend(friend.id.toString(), user.id.toString(), object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("INVITES", jsonObject.toString())
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "FriendsList - onError")
                arrayList.remove(friend)

                if(arrayList.isEmpty()){
                    noneTextView.visibility = View.VISIBLE
                }

                notifyDataSetChanged()
            }
        })
    }
}