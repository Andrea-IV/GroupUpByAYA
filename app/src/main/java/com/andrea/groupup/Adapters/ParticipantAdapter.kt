package com.andrea.groupup.Adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.andrea.groupup.GroupActivity
import com.andrea.groupup.Http.GroupHttp
import com.andrea.groupup.Http.Http
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Models.User
import com.andrea.groupup.R
import com.android.volley.VolleyError
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class ParticipantAdapter(items: ArrayList<User>, user: User, IsAdmin: Boolean, IdGroup: Int, Token: String, ctx: Context) :
    ArrayAdapter<User>(ctx,
        R.layout.list_of_participants, items) {

    private lateinit var http: Http
    var userConnected = user
    var isAdmin = IsAdmin
    var arrayList = items
    var tempList = ArrayList(arrayList)
    var token = Token
    var idGroup = IdGroup

    //view holder is used to prevent findViewById calls
    private class ParticipantViewHolder {
        internal var image: ImageView? = null
        internal var name: TextView? = null
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view

        val viewHolder: ParticipantViewHolder

        http = Http(context)
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

        if(participant.UserGroup.is_admin){
            val adminImage: ImageView = view.findViewById(R.id.isAdmin)
            adminImage.visibility = View.VISIBLE
        }else{
            val adminImage: ImageView = view.findViewById(R.id.isAdmin)
            adminImage.visibility = View.INVISIBLE
        }

        if(userConnected.id == participant.id){
            val imageView: ImageView = view.findViewById(R.id.imageView)
            imageView.setOnClickListener {
                val popupMenu = PopupMenu(context, it)
                popupMenu.setOnMenuItemClickListener { item ->
                    when(item.itemId){
                        R.id.leave -> {
                            GroupHttp(http).leaveGroup(idGroup.toString(), token, object:VolleyCallback {
                                override fun onResponse(jsonObject: JSONObject) {
                                    Log.d("LEAVE OK", jsonObject.toString())
                                    (context as Activity).finish()
                                }

                                override fun onError(error: VolleyError) {
                                    Log.e("LEAVE", "leave group - onError")
                                    //Log.e("LEAVE", error.toString())
                                    val intent = Intent(context, GroupActivity::class.java)
                                    intent.putExtra("User", userConnected)
                                    intent.putExtra("Token", token)
                                    context.startActivity(intent)
                                }
                            })
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
        }else{
            if(isAdmin){
                val imageView: ImageView = view.findViewById(R.id.imageView)
                imageView.setOnClickListener {
                    val popupMenu = PopupMenu(context, it)
                    popupMenu.setOnMenuItemClickListener { item ->
                        when(item.itemId){
                            R.id.leave -> {
                                GroupHttp(http).kickGroup(idGroup.toString(), participant.id.toString(), token, object:VolleyCallback {
                                    override fun onResponse(jsonObject: JSONObject) {
                                        Log.d("LEAVE", jsonObject.toString())
                                    }

                                    override fun onError(error: VolleyError) {
                                        Log.e("LEAVE", "leave group - onError")
                                        arrayList.removeAt(i)
                                        notifyDataSetChanged()
                                    }
                                })
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
            }else{
                val imageView: ImageView = view.findViewById(R.id.imageView)
                imageView.visibility = View.INVISIBLE
            }
        }

        view.tag = viewHolder

        return view
    }

    fun filter(text: String?) {
        val text = text!!.toLowerCase(Locale.getDefault())
        arrayList.clear()

        if (text.length == 0) {
            arrayList.addAll(tempList)
        } else {
            for (i in 0..tempList.size - 1) {
                if (tempList[i].username!!.toLowerCase(Locale.getDefault()).contains(text)) {
                    arrayList.add(tempList.get(i))
                }
            }
        }

        notifyDataSetChanged()
    }
}