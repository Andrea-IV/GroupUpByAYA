package com.andrea.groupup.Fragments

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.andrea.groupup.Adapters.AddParticipantAdapter
import com.andrea.groupup.Adapters.ParticipantAdapter
import com.andrea.groupup.Constants
import com.andrea.groupup.DetailsActivity
import com.andrea.groupup.Http.*
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User

import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import net.gotev.uploadservice.MultipartUploadRequest
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.properties.Delegates

/**
 * A simple [Fragment] subclass.
 */
class GroupFragment : BaseFragment() {
    private val REQUEST_CODE = 100

    private lateinit var dialog: BottomSheetDialog
    lateinit var group: Group
    lateinit var user: User
    lateinit var token: String
    var isAdmin: Boolean = false
    lateinit var mView: View

    lateinit var addAdapter: AddParticipantAdapter
    lateinit var userRes: List<User>
    var addListItems = arrayListOf<User>()
    private var permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var storagePermissionGranted = false

    private var uri: Uri? = null
    private val uploadReceiver = SingleUploadBroadcastReceiver()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_group, container, false)

        group = ACTIVITY.group
        user = ACTIVITY.user
        token = ACTIVITY.token

        val listItems = arrayListOf<User>()

        for (member in group.members) {
            if(user.id == member.id){
                isAdmin = member.UserGroup.is_admin
            }
            listItems.add(member)
        }

        val adapter = ParticipantAdapter(listItems, user, isAdmin, group.id, token, requireContext())
        val listView: ListView = mView.findViewById(R.id.listOfParticipants)
        listView.adapter = adapter

        mView.findViewById<ImageView>(R.id.editGroup).setOnClickListener {
            initEdit()
        }

        if(!user.UserGroup.is_admin){
            mView.findViewById<ImageView>(R.id.editGroup).visibility = View.GONE
        }

        val addUser: ImageView = mView.findViewById(R.id.addUser)

        addUser.setOnClickListener{
            UserHttp(ACTIVITY).getAll(object: VolleyCallbackArray {
                override fun onResponse(array: JSONArray) {
                    val dialog = BottomSheetDialog(ACTIVITY, R.style.DialogStyle)

                    val view = layoutInflater.inflate(R.layout.dialog_add_user, null)
                    val textView: TextView = view.findViewById(R.id.userNone)
                    textView.visibility = View.GONE
                    addListItems.clear()

                    userRes = Mapper().mapper(array)
                    for (user: User in userRes){
                        addListItems.add(user)
                    }
                    addListItems.removeAll(listItems)

                    if(addListItems.isEmpty()){
                        textView.visibility = View.VISIBLE
                    }
                    addAdapter = AddParticipantAdapter(addListItems, requireContext())
                    val addListView: ListView = view.findViewById(R.id.addListOfParticipants)
                    addListView.adapter = addAdapter

                    addListView.onItemClickListener = object : AdapterView.OnItemClickListener {
                        override fun onItemClick(parent: AdapterView<*>, view: View,
                                                 position: Int, id: Long) {
                            addListItems[position]
                            GroupHttp(ACTIVITY).addToGroup(group.id.toString(), addListItems[position].id.toString(), token, object:VolleyCallback {
                                override fun onResponse(jsonObject: JSONObject) {
                                    val gson: Gson = Gson()
                                    val listGroup = arrayListOf<Group>()
                                    listGroup.add(gson.fromJson(jsonObject.toString(), Group::class.java))

                                    listItems.clear()
                                    for (member in listGroup[0].members) {
                                        listItems.add(member)
                                    }

                                    listView.invalidateViews();
                                    dialog.dismiss()
                                }

                                override fun onError(error: VolleyError) {
                                    Log.e("ADD TO GROUP", "add to group - onError")
                                    Log.e("ADD TO GROUP", error.toString())
                                }
                            })
                        }
                    }

                    // SEARCH INIT
                    val searchView: SearchView = view.findViewById(R.id.searchBar)
                    searchView.queryHint = getString(R.string.searchHint)
                    searchView.imeOptions = EditorInfo.IME_ACTION_DONE

                    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String): Boolean {
                            return false
                        }

                        override fun onQueryTextChange(newText: String): Boolean {
                            addAdapter.filter(newText)
                            return false
                        }
                    })
                    dialog.setContentView(view)
                    dialog.show()
                }


                override fun onError(error: VolleyError) {
                    Log.e("USER", "login - onError")
                    Log.e("USER", error.toString())
                }
            })
        }

        /*listView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(requireContext(), EventActivity::class.java)
                startActivity(intent)
            }
        }*/
        return mView
    }

    fun initEdit(){
        dialog = BottomSheetDialog(this.requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_edit_group, null)
        dialog.setContentView(view)

        view.findViewById<ImageView>(R.id.profile_image).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE)
        }

        /*if(!group.pp_link.contains("base")){
            Picasso.get().load(Constants.BASE_URL + "/" + user.pp_link).into(view.findViewById<ImageView>(R.id.profile_image))
        }*/
        Log.d("PICTURE", group.picture)

        val name: EditText = view.findViewById(R.id.name_value)
        name.hint = group.name

        view.findViewById<Button>(R.id.editButton).setOnClickListener {
            editGroup(name.text.toString())
        }
        dialog.show()
    }

    fun editGroup(name: String){
        if(name.isNotEmpty() && name.isNotBlank()){
            editAction(name)
        }
        if(uri != null){
            (this.activity as DetailsActivity).startUpload(uri!!)
        }
        dialog.dismiss()
    }

    private fun editAction(name: String){
        GroupHttp(this.requireContext()).editGroup(group.id.toString(), name, token, object: VolleyCallback {
            override fun onResponse(jsonObject: JSONObject) {
                Log.d("EDIT TRY", jsonObject.toString())
                group.name = name
            }

            override fun onError(error: VolleyError) {
                Log.e("EDIT TRY", "edit - onError")
                Log.e("EDIT TRY", error.toString())
            }
        })
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

    private fun checkPermission() : Boolean {
        Log.d("PERMISSION", "checkPermission")
        return (ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.requireActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        Log.d("PERMISSION", "requestPermissions")
        ActivityCompat.requestPermissions(this.requireActivity(), permissions,1)
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
}
