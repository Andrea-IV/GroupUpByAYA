package com.andrea.groupup

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Adapters.GroupAdapter
import com.andrea.groupup.Adapters.InvitesAdapter
import com.andrea.groupup.Http.FriendHttp
import com.andrea.groupup.Http.GroupHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallback
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.FriendRequest
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import kotlin.collections.ArrayList


class GroupActivity : AppCompatActivity() {

    private lateinit var adapter: GroupAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private lateinit var user: User
    private lateinit var token: String
    private lateinit var context: Context
    private var listItems: ArrayList<Group> = arrayListOf<Group>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        user = intent.getSerializableExtra("User") as User
        token = intent.getStringExtra("Token")

        context = this

        groupViewInit()
        searchInit()
        addGroupInit()
        menuInit()
    }

    private fun groupViewInit(){
        GroupHttp(this).getGroupForUser(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("GROUP", array.toString())
                val groupRes = Mapper().mapper<JSONArray, List<Group>>(array)
                for (group: Group in groupRes){
                    listItems.add(group)
                }

                adapter = GroupAdapter(listItems, context)

                gridView = findViewById(R.id.listOfGroups)
                gridView.adapter = adapter

                gridView.onItemClickListener = object : AdapterView.OnItemClickListener {
                    override fun onItemClick(parent: AdapterView<*>, view: View,
                                             position: Int, id: Long) {
                        val intent = Intent(this@GroupActivity, DetailsActivity::class.java)
                        intent.putExtra("Group", listItems[position])
                        intent.putExtra("User", user)
                        intent.putExtra("Token", token)
                        startActivity(intent)
                    }
                }
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.toString())
            }
        })
    }

    private fun searchInit(){
        searchView = findViewById(R.id.searchBar)
        searchView.queryHint = getString(R.string.searchHint)
        searchView.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                adapter.filter(newText)
                return false
            }
        })
    }

    private fun addGroupInit(){
        val addGroup: ImageView = findViewById(R.id.addGroup)

        addGroup.setOnClickListener{
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.dialog_create_group, null)
            dialog.setContentView(view)

            val createButton: Button = view.findViewById(R.id.button3)
            createButton.setOnClickListener {
                if(view.findViewById<EditText>(R.id.newGroup).text.isNotEmpty()){
                    view.findViewById<TextView>(R.id.error).visibility = View.GONE

                    Log.d("GROUP", token)

                    GroupHttp(this).createGroup(view.findViewById<EditText>(R.id.newGroup).text.toString(), token, object: VolleyCallback {
                        override fun onResponse(jsonObject: JSONObject) {
                            Log.d("GROUP", jsonObject.toString())
                            val gson:Gson = Gson()
                            listItems.add(gson.fromJson(jsonObject.toString(), Group::class.java))
                            gridView.invalidateViews();
                            dialog.dismiss()
                        }

                        override fun onError(error: VolleyError) {
                            Log.e("USER", "login - onError")
                            Log.e("USER", error.toString())
                        }
                    })
                }else{
                    view.findViewById<TextView>(R.id.error).text = getString(R.string.errorEmpty)
                    view.findViewById<TextView>(R.id.error).visibility = View.VISIBLE
                }
            }

            dialog.show()
        }
    }

    private fun menuInit(){
        val menu: ImageView = findViewById(R.id.menu)

        menu.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.profile -> {
                        profileDialogInit(this)
                        true
                    }
                    R.id.notification -> {
                        invitesDialogInit(this)
                        true
                    }
                    R.id.friend -> {
                        goToFriends()
                        true
                    }
                    else -> false
                }
            }

            popupMenu.inflate(R.menu.menu_profile)

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
    }

    private fun invitesDialogInit(context: Context){
        val dialog = BottomSheetDialog(context)
        val view = layoutInflater.inflate(R.layout.dialog_friend_invites, null)

        FriendHttp(this).getFriendRequests(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                val requestRes = Mapper().mapper<JSONArray, List<FriendRequest>>(array)
                val requestItems: ArrayList<User> = arrayListOf()
                val noneTextView: TextView = view.findViewById(R.id.invitesNone)

                for (request: FriendRequest in requestRes){
                    requestItems.add(request.User)
                }

                if(!requestItems.isEmpty()){
                   noneTextView.visibility = View.GONE
                }

                val requestAdapter = InvitesAdapter(requestItems, user, token, noneTextView, context)

                val listOfInvites: ListView = view.findViewById(R.id.listOfFriends)
                listOfInvites.adapter = requestAdapter
            }

            override fun onError(error: VolleyError) {
                Log.e("INVITES", "Invites - onError")
                Log.e("INVITES", error.toString())
            }
        })

        dialog.setContentView(view)
        dialog.show()
    }

    private fun profileDialogInit(context: Context){
        val dialog = BottomSheetDialog(context)
        val view = layoutInflater.inflate(R.layout.dialog_profile, null)
        dialog.setContentView(view)

        val username: EditText = view.findViewById(R.id.username)
        username.hint = user.username
        val firstName: EditText = view.findViewById(R.id.firstname_value)
        firstName.hint = user.firstname
        val lastName: EditText = view.findViewById(R.id.lastname_value)
        lastName.hint = user.lastname
        val email: EditText = view.findViewById(R.id.email_value)
        email.hint = user.email
        val password: EditText = view.findViewById(R.id.password_value)
        password.hint = this.resources.getString(R.string.old_password)
        val newPassword: EditText = view.findViewById(R.id.new_password_value)
        newPassword.hint = this.resources.getString(R.string.new_password)
        val confirmPassword: EditText = view.findViewById(R.id.confirm_password_value)
        confirmPassword.hint = this.resources.getString(R.string.confirm_password)

        dialog.show()
    }

    private fun goToFriends(){
        val intent = Intent(this@GroupActivity, FriendsActivity::class.java)
        intent.putExtra("User", user)
        intent.putExtra("Token", token)
        startActivity(intent)
    }
}
