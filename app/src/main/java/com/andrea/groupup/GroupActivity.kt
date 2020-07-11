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
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
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

        adapter = GroupAdapter(listItems, context)

        gridView = findViewById(R.id.listOfGroups)
        gridView.adapter = adapter

        groupViewInit()
        searchInit()
        addGroupInit()
        menuInit()
    }

    override fun onResume() {
        super.onResume()
        groupViewInit()
    }
    private fun groupViewInit(){
        GroupHttp(this).getGroupForUser(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("GROUP", array.toString())
                listItems.clear()
                val groupRes = Mapper().mapper<JSONArray, List<Group>>(array)
                for (group: Group in groupRes){
                    listItems.add(group)
                }
                adapter.notifyDataSetChanged()

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
        val email: EditText = view.findViewById(R.id.email_value)
        email.hint = user.email
        val password: EditText = view.findViewById(R.id.password_value)
        password.hint = this.resources.getString(R.string.old_password)
        val newPassword: EditText = view.findViewById(R.id.new_password_value)
        newPassword.hint = this.resources.getString(R.string.new_password)
        val confirmPassword: EditText = view.findViewById(R.id.confirm_password_value)
        confirmPassword.hint = this.resources.getString(R.string.confirm_password)

        view.findViewById<Button>(R.id.editButton).setOnClickListener {
            editProfile(username.text.toString(), email.text.toString(), password.text.toString(), newPassword.text.toString(), confirmPassword.text.toString(), dialog)
        }
        dialog.show()
    }

    private fun editProfile(username: String, email: String, password: String, newPassword: String, confirmPassword:String, dialog: BottomSheetDialog){
        var usernameToSend = ""
        var emailToSend = ""
        var passwordToSend = false
        var error = false

        if(username.isNotEmpty() || username.isNotBlank()){
            usernameToSend = username.trim()
        }

        if(email.isNotEmpty() || email.isNotBlank()){
            emailToSend = email.trim()
        }

        if(password.isNotEmpty() || password.isNotBlank() || newPassword.isNotEmpty() || newPassword.isNotBlank() || confirmPassword.isNotEmpty() || confirmPassword.isNotBlank()){
            if(newPassword == confirmPassword){
                passwordToSend = true
            }else{
                error = true
                dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.errorSamePassword)
                dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
            }
        }

        if(!error){
            editAction(usernameToSend, emailToSend, passwordToSend, password, newPassword, dialog)
        }
    }

    private fun editAction(usernameToSend: String, emailToSend: String, passwordToSend: Boolean, password: String, newPassword: String, dialog: BottomSheetDialog){
        if(passwordToSend){
            tryLoginBefore(usernameToSend, emailToSend, passwordToSend, password, newPassword, dialog)
        }else{
            val params = fillParams(usernameToSend, emailToSend, passwordToSend, newPassword)
            callEdit(params, usernameToSend, emailToSend, dialog)
        }
    }

    private fun tryLoginBefore(usernameToSend: String, emailToSend: String, passwordToSend: Boolean, password: String, newPassword: String, dialog: BottomSheetDialog){
        UserHttp(this).baseLogin(user.username, password, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("LOGIN TRY", jsonObject.toString())
                token = jsonObject.get("token").toString()
                token = token.substring(token.indexOf(" ") + 1, token.length)

                val params = fillParams(usernameToSend, emailToSend, passwordToSend, newPassword)
                callEdit(params, usernameToSend, emailToSend, dialog)
            }

            override fun onError(error: VolleyError) {
                Log.e("LOGIN TRY", "login - onError")
                Log.e("LOGIN TRY", error.toString())
                dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.error_login)
                dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
            }
        })
    }

    private fun callEdit(params: String, usernameToSend: String, emailToSend: String, dialog: BottomSheetDialog){
        Log.d("PARAMS", params)
        UserHttp(context).editUser(token, params, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("EDIT TRY", jsonObject.toString())
                val gson = Gson()
                user = gson.fromJson(jsonObject.toString(), User::class.java)
            }

            override fun onError(error: VolleyError) {
                Log.e("EDIT TRY", "login - onError")
                Log.e("EDIT TRY", error.toString())
                if(error.toString().contains("type org.json.JSONArray cannot be converted to JSONObject")){
                    if(usernameToSend.isNotEmpty() && usernameToSend.isNotBlank()){
                        user.username = usernameToSend
                    }
                    if(emailToSend.isNotEmpty() && emailToSend.isNotBlank()){
                        user.email = emailToSend
                    }
                    dialog.dismiss()
                }else{
                    dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.error_login)
                    dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun fillParams(usernameToSend: String, emailToSend: String, passwordToSend: Boolean, newPassword: String): String{
        var params = "{\"id\": ${user.id},"
        if(usernameToSend.isNotEmpty()){
            params += "\"username\":\"$usernameToSend\","
        }
        if(emailToSend.isNotEmpty()){
            params += "\"email\":\"$emailToSend\","
        }
        if(passwordToSend){
            params += "\"password\":\"$newPassword\","
        }

        params = params.substring(0,params.lastIndex) + "}"
        return params
    }

    private fun goToFriends(){
        val intent = Intent(this@GroupActivity, FriendsActivity::class.java)
        intent.putExtra("User", user)
        intent.putExtra("Token", token)
        startActivity(intent)
    }
}
