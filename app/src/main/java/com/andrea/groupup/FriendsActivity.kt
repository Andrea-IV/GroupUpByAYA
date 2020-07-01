package com.andrea.groupup

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import com.andrea.groupup.Adapters.AddParticipantAdapter
import com.andrea.groupup.Adapters.FriendAdapter
import com.andrea.groupup.Adapters.ParticipantAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

lateinit var addAdapter: AddParticipantAdapter
lateinit var userRes: List<User>
var addListItems = arrayListOf<User>()

lateinit var user: User
lateinit var token: String
lateinit var context: Context

class FriendsActivity : AppCompatActivity() {
    var listItems: ArrayList<User> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friends)

        user = intent.getSerializableExtra("User") as User
        token = intent.getStringExtra("Token")

        friendListInit()

        context = this

        addUserInit()

    }

    private fun friendListInit(){
        FriendHttp(this).getFriend(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                val noFriendsTextView: TextView = findViewById(R.id.friendsNone)
                val gson = Gson()
                listItems.clear()

                for(i in 0 until array.length()) {
                    val jsonObject: JSONObject = array.getJSONObject(i).get("User") as JSONObject
                    val friend = gson.fromJson(jsonObject.toString(), User::class.java)
                    listItems.add(friend)
                }

                if(listItems.isEmpty()){
                    noFriendsTextView.visibility = View.VISIBLE
                }else{
                    noFriendsTextView.visibility = View.GONE
                }

                val adapter = FriendAdapter(listItems, user, context)
                val listView: ListView = findViewById(R.id.listOfFriends)
                listView.adapter = adapter
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "FriendsList - onError")
                Log.e("USER", error.toString())
            }
        })
    }

    private fun addUserInit(){
        val addUser: ImageView = findViewById(R.id.addUser)

        addUser.setOnClickListener{
            UserHttp(this).getAll(object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    val dialog = BottomSheetDialog(context, R.style.DialogStyle)
                    val viewDialog = layoutInflater.inflate(R.layout.dialog_add_friend, null)
                    dialog.setContentView(viewDialog)

                    addListItems.clear()

                    userRes = Mapper().mapper(array)
                    for (user: User in userRes){
                        addListItems.add(user)
                    }
                    addListItems.removeAll(listItems)

                    val addButton: Button = viewDialog.findViewById(R.id.addFriend)
                    val username: EditText = viewDialog.findViewById(R.id.editText)
                    val error: TextView = viewDialog.findViewById(R.id.error)
                    error.visibility = View.GONE

                    addButton.setOnClickListener {
                        var found = userExist(username.text.toString())
                        if(found == -1){
                            error.visibility = View.VISIBLE
                            error.text = getString(R.string.userNotExist)
                        }else{
                            verifyStatus(found, error, dialog)
                        }
                    }

                    dialog.show()
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.toString())
                }
            })
        }
    }

    private fun userExist(username: String): Int{
        var found: Int = -1
        for (i in 0..addListItems.size - 1) {
            if (addListItems[i].username!!.toLowerCase(Locale.getDefault()) == username.toLowerCase()) {
                found = i
            }
        }

        return found
    }

    private fun verifyStatus(position: Int, error: TextView, dialog: BottomSheetDialog){
        FriendHttp(context).getFriendStatus(user.id.toString(), addListItems[position].id.toString(), object:VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                error.visibility = View.VISIBLE
                error.text = getString(R.string.invitationStillProcessing)
            }

            override fun onError(error: VolleyError) {
                inviteUser( addListItems[position].id, dialog)
            }
        })
    }

    private fun inviteUser(idInvite: Int, dialog: BottomSheetDialog){
        FriendHttp(context).addToFriend(idInvite.toString(), token, object:VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("Invite", jsonObject.toString())
                dialog.dismiss()
            }

            override fun onError(error: VolleyError) {
                Log.d("Invite", "NOT SENT")
            }
        })
    }
}
