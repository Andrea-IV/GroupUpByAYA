package com.andrea.groupup

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.andrea.groupup.Adapters.GroupAdapter
import com.andrea.groupup.Adapters.InvitesAdapter
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.FriendRequest
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import com.android.volley.VolleyError
import com.facebook.login.LoginManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList


class GroupActivity : AppCompatActivity(), SingleUploadBroadcastReceiver.Delegate {
    private val REQUEST_CODE = 100

    private lateinit var dialog: BottomSheetDialog
    private lateinit var adapter: GroupAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private lateinit var user: User
    private lateinit var token: String
    private lateinit var context: Context
    private var listItems: ArrayList<Group> = arrayListOf<Group>()
    private var facebookLogin = false

    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false

    private var uri: Uri? = null
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    private val groupInfoRunnable =  object: Runnable {
        override fun run() {
            getGroupInfo()
            infoGroupHandler.postDelayed(this, 5000)
        }
    }
    private lateinit var infoGroupHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        user = intent.getSerializableExtra("User") as User
        token = intent.getStringExtra("Token")
        facebookLogin = intent.getBooleanExtra("FacebookLogin", false)

        context = this

        adapter = GroupAdapter(listItems, context)

        gridView = findViewById(R.id.listOfGroups)
        gridView.adapter = adapter

        groupViewInit()
        searchInit()
        addGroupInit()
        menuInit()

        infoGroupHandler = Handler(Looper.getMainLooper())
        infoGroupHandler.post(groupInfoRunnable)
    }

    private fun groupViewInit(){
        GroupHttp(this).getGroupForUser(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("GROUP", array.toString())
                displayGroups(array)
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
                Log.d("FILTER", newText)
                adapter.filter(newText)
                return false
            }
        })
    }

    private fun addGroupInit(){
        val addGroup: ImageView = findViewById(R.id.addGroup)

        addGroup.setOnClickListener{
            dialog = BottomSheetDialog(this)
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
                    R.id.logout -> {
                        logout()
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
        dialog = BottomSheetDialog(context)
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
        dialog = BottomSheetDialog(context)
        val view = layoutInflater.inflate(R.layout.dialog_profile, null)
        dialog.setContentView(view)

        view.findViewById<ImageView>(R.id.profile_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        if(!user.pp_link.contains("base")){
            Picasso.get().load(Constants.BASE_URL + "/" + user.pp_link).into(view.findViewById<ImageView>(R.id.profile_image))
        }

        val username: EditText = view.findViewById(R.id.username)
        username.hint = user.username
        val password: EditText = view.findViewById(R.id.password_value)
        password.hint = this.resources.getString(R.string.old_password)
        val newPassword: EditText = view.findViewById(R.id.new_password_value)
        newPassword.hint = this.resources.getString(R.string.new_password)
        val confirmPassword: EditText = view.findViewById(R.id.confirm_password_value)
        confirmPassword.hint = this.resources.getString(R.string.confirm_password)

        view.findViewById<Button>(R.id.editButton).setOnClickListener {
            editProfile(username.text.toString(), password.text.toString(), newPassword.text.toString(), confirmPassword.text.toString(), dialog)
        }
        dialog.show()
    }

    private fun editProfile(username: String, password: String, newPassword: String, confirmPassword:String, dialog: BottomSheetDialog){
        var usernameToSend = ""
        var passwordToSend = false
        var error = false

        if(username.isNotEmpty() || username.isNotBlank()){
            usernameToSend = username.trim()
        }

        if(password.isNotEmpty() && password.isNotBlank() && newPassword.isNotEmpty() && newPassword.isNotBlank() && confirmPassword.isNotEmpty() && confirmPassword.isNotBlank()){
            if(newPassword == confirmPassword){
                passwordToSend = true
            }else{
                error = true
                dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.errorSamePassword)
                dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
            }
        }

        if(!error){
            editAction(usernameToSend, passwordToSend, password, newPassword, dialog)
        }
    }

    private fun editAction(usernameToSend: String, passwordToSend: Boolean, password: String, newPassword: String, dialog: BottomSheetDialog){
        var editDone = false
        if(passwordToSend){
            editDone = true
            tryLoginBefore(usernameToSend, passwordToSend, password, newPassword, dialog)
        }else{
            editDone = true
            val params = fillParams(usernameToSend, passwordToSend, newPassword)
            callEdit(params, usernameToSend, dialog)
        }
        if(uri != null){
            if(!editDone){
                dialog.dismiss()
            }
            startUpload()
        }
    }

    private fun tryLoginBefore(usernameToSend: String, passwordToSend: Boolean, password: String, newPassword: String, dialog: BottomSheetDialog){
        UserHttp(this).baseLogin(user.username, password, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("LOGIN TRY", jsonObject.toString())
                token = jsonObject.get("token").toString()
                token = token.substring(token.indexOf(" ") + 1, token.length)

                val params = fillParams(usernameToSend, passwordToSend, newPassword)
                callEdit(params, usernameToSend, dialog)
            }

            override fun onError(error: VolleyError) {
                Log.e("LOGIN TRY", "login - onError")
                Log.e("LOGIN TRY", error.toString())
                dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.error_login)
                dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
            }
        })
    }

    private fun callEdit(params: String, usernameToSend: String, dialog: BottomSheetDialog){
        Log.d("PARAMS", params)
        UserHttp(context).editUser(token, params, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("EDIT TRY", jsonObject.toString())
                val gson = Gson()
                user = gson.fromJson(jsonObject.toString(), User::class.java)
                dialog.dismiss()
            }

            override fun onError(error: VolleyError) {
                Log.e("EDIT TRY", "login - onError")
                Log.e("EDIT TRY", error.toString())
                if(error.toString().contains("type org.json.JSONArray cannot be converted to JSONObject")){
                    if(usernameToSend.isNotEmpty() && usernameToSend.isNotBlank()){
                        user.username = usernameToSend
                    }
                    dialog.dismiss()
                }else{
                    dialog.findViewById<TextView>(R.id.error)?.text = getString(R.string.error_login)
                    dialog.findViewById<TextView>(R.id.error)?.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun fillParams(usernameToSend: String, passwordToSend: Boolean, newPassword: String): String{
        var params = "{\"id\": ${user.id},"
        if(usernameToSend.isNotEmpty()){
            params += "\"username\":\"$usernameToSend\","
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE){
            uri = data?.data

            if(checkPermission()) {
                storagePermissionGranted = true
            } else {
                requestPermissions()
            }

            dialog.findViewById<CircleImageView>(R.id.profile_image)?.setImageURI(data?.data)
        }
    }

    private fun startUpload(){
        val uploadId = UUID.randomUUID().toString()
        uploadReceiver.setDelegate(this)
        uploadReceiver.setUploadID(uploadId)

        MultipartUploadRequest(this, uploadId, Constants.BASE_URL + "/users/picture")
            .addFileToUpload(getUriPath(), "picture") //Adding file
            .addHeader("Authorization", "Bearer $token")
            .setMaxRetries(2)
            .startUpload()
    }


    private fun getUriPath(): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = managedQuery(uri, projection, null, null, null)
        startManagingCursor(cursor)
        val column_index: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(column_index)
    }

    override fun onResume() {
        super.onResume()
        infoGroupHandler.post(groupInfoRunnable)
        uploadReceiver.register(this)
    }

    override fun onPause() {
        super.onPause()
        infoGroupHandler.removeCallbacks(groupInfoRunnable)
        uploadReceiver.unregister(this)
    }

    override fun onProgress(progress: Int) {
        //your implementation
    }

    override fun onProgress(uploadedBytes: Long, totalBytes: Long) {
        //your implementation
    }

    override fun onError(exception: java.lang.Exception?) {
        Log.e("ERROR", exception.toString())
    }

    override fun onCompleted(serverResponseCode: Int, serverResponseBody: ByteArray?) {
        if(serverResponseCode.toString() == "201"){
            UserHttp(this).getByName(user.username, object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    Log.d("USER", array.toString())
                    user.pp_link = (array[0] as JSONObject)["pp_link"].toString()
                }

                override fun onError(error: VolleyError) {
                    Log.e("USER", "get photo - onError")
                    Log.e("USER", error.toString())
                }
            })
        }
    }

    override fun onCancelled() {
        TODO("Not yet implemented")
    }

    private fun checkPermission() : Boolean {
        Log.d("PERMISSION", "checkPermission")
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        Log.d("PERMISSION", "requestPermissions")
        ActivityCompat.requestPermissions(this, permissions,1)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d("PERMISSION", "onRequestPermissionsResult")
        if(requestCode == 1) {
            if(grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if(it != PackageManager.PERMISSION_GRANTED) {
                        storagePermissionGranted = false
                        return
                    }
                }
                storagePermissionGranted = true;
            }
        }
    }

    private fun getGroupInfo(){
        GroupHttp(this).getGroupForUser(user.id.toString(), object: VolleyCallbackArray {
            override fun onResponse(array: JSONArray) {
                Log.d("GROUP", array.toString())
                val verifyGroup =  ArrayList<Group>()
                val groupRes = Mapper().mapper<JSONArray, List<Group>>(array)

                for (group: Group in groupRes){
                    verifyGroup.add(group)
                }

                if(isChanged(verifyGroup)){
                    displayGroups(array)
                }
            }

            override fun onError(error: VolleyError) {
                Log.e("USER", "login - onError")
                Log.e("USER", error.toString())
            }
        })
    }

    private fun isChanged(verifyGroup: ArrayList<Group>): Boolean{
        if(verifyGroup.size != listItems.size){
            return true
        }
        if(listItems.equals(verifyGroup)){
            return true
        }
        return false
    }

    private fun displayGroups(array: JSONArray){
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

    fun logout() {
        if (facebookLogin) {
            LoginManager.getInstance().logOut()
        }
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}
