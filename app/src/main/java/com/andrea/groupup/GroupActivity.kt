package com.andrea.groupup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.andrea.groupup.Adapters.GroupAdapter
import com.andrea.groupup.Models.Group
import com.google.android.material.bottomsheet.BottomSheetDialog


class GroupActivity : AppCompatActivity() {

    private lateinit var adapter: GroupAdapter
    private lateinit var searchView: SearchView
    private lateinit var gridView: GridView
    private var listItems: ArrayList<Group> = arrayListOf<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group)

        for (i in 1 until 8) {
            listItems.add(
                Group(i, "The " + i + " Group", 3, 0)
            )
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

                    listItems.add(Group(9, view.findViewById<EditText>(R.id.newGroup).text.toString(), 1, 0))
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
                        Toast.makeText(this, "Profile", Toast.LENGTH_LONG).show()
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
