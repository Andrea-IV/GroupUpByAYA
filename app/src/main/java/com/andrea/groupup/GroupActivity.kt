package com.andrea.groupup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Adapters.GroupAdapter
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Models.Group
import com.andrea.groupup.Models.User
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject


class GroupActivity : AppCompatActivity() {

    private lateinit var adapter: GroupAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private lateinit var user: User
    private var listItems: ArrayList<Group> = arrayListOf<Group>()
    private var groups = HashMap<Int, Group>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        val userString:String = intent.getStringExtra("User")
        val gson:Gson = Gson()
        val firstIndex:Int =  userString.indexOf("\"groups\":") + 9
        val secondIndex:Int =  userString.indexOf("]") + 1

        Log.d("GROUP", userString.substring(firstIndex, secondIndex))
        val groupJson = JSONArray(userString.substring(firstIndex, secondIndex))

        user = gson.fromJson(userString, User::class.java)
        val groupJsonArray = Mapper().mapper<JSONArray, List<Group>>(groupJson)
        for(group in groupJsonArray) {
            listItems.add(group)
        }

        adapter = GroupAdapter(listItems, this)

        gridView = findViewById(R.id.listOfGroups)
        gridView.adapter = adapter

        gridView.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(parent: AdapterView<*>, view: View,
                                     position: Int, id: Long) {
                val intent = Intent(this@GroupActivity, DetailsActivity::class.java)
                startActivity(intent)
            }
        }

        // SEARCH INIT
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

        //ADD GROUP INIT
        val addGroup: ImageView = findViewById(R.id.addGroup)

        addGroup.setOnClickListener{
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.dialog_create_group, null)
            dialog.setContentView(view)

            val createButton: Button = view.findViewById(R.id.button3)
            createButton.setOnClickListener {
                if(view.findViewById<EditText>(R.id.newGroup).text.isNotEmpty()){
                    view.findViewById<TextView>(R.id.error).visibility = View.GONE

                    //listItems.add(Group(9, view.findViewById<EditText>(R.id.newGroup).text.toString(), 1, 0))
                    gridView.invalidateViews();
                    dialog.dismiss()
                }else{
                    view.findViewById<TextView>(R.id.error).text = getString(R.string.errorEmpty)
                    view.findViewById<TextView>(R.id.error).visibility = View.VISIBLE
                }
            }

            dialog.show()
        }

        // MENU INIT
        val menu: ImageView = findViewById(R.id.menu)

        menu.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.setOnMenuItemClickListener { item ->
                when(item.itemId){
                    R.id.profile -> {
                        val dialog = BottomSheetDialog(this)
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
                        true
                    }
                    R.id.notification -> {
                        Toast.makeText(this, "Notification", Toast.LENGTH_LONG).show()
                        true
                    }
                    R.id.friend -> {
                        Toast.makeText(this, "Friend", Toast.LENGTH_LONG).show()
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
}
