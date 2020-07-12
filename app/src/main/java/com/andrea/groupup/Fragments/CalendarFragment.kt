package com.andrea.groupup.Fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.andrea.groupup.Adapters.EventAdapter
import com.andrea.groupup.EventActivity
import com.andrea.groupup.Http.EventHttp
import com.andrea.groupup.Http.Mapper.Mapper
import com.andrea.groupup.Http.VolleyCallbackArray
import com.andrea.groupup.Models.*
import com.andrea.groupup.R
import com.android.volley.VolleyError
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class CalendarFragment : BaseFragment() {
    private lateinit var token: String
    private lateinit var group: Group
    private lateinit var user: User
    private lateinit var listItems: ArrayList<EventDisplay>
    private lateinit var mView: View
    private lateinit var adapter: EventAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mView = inflater.inflate(R.layout.fragment_calendar, container, false)
        listItems = arrayListOf<EventDisplay>()

        group = ACTIVITY.group
        user = ACTIVITY.user
        token = ACTIVITY.token

        adapter = EventAdapter(listItems, requireContext())
        val listView: ListView = mView.findViewById(R.id.listOfEvents)
        listView.adapter = adapter

        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val intent = Intent(this.requireContext(), EventActivity::class.java)
            Log.d("EVENT", listItems[position].toString())
            intent.putExtra("EVENTDISPLAY", listItems[position])
            intent.putExtra("GROUP", group)
            intent.putExtra("USER", user)
            intent.putExtra("TOKEN", token)
            startActivityForResult(intent, 0);
        }

        initEvents()

        return mView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        initEvents()
    }

    private fun initEvents(){
        EventHttp(this.requireContext()).getEvents(group.id.toString(), token, object: VolleyCallbackArray {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(array: JSONArray) {
                val lpRes = Mapper().mapper<JSONArray, List<Event>>(array)
                listItems.clear()

                for(i: Int in 0 until array.length()){
                    val item =  array.getJSONObject(i)
                    for (j: Int in 0 until (item["LocalPlaces"] as JSONArray).length()){
                        lpRes[i].LocalPlaces[j].pos = (((item["LocalPlaces"] as JSONArray)[j] as JSONObject)["TravelLocalplace"]as JSONObject)["position"] as Int
                    }
                }

                for(event in lpRes) {
                    var found = false
                    event.travel_date_original = event.travel_date
                    val userEvent = findUser(event.UserId)
                    if(userEvent != null){
                        for(i: Int in 0 until listItems.size){
                            if(listItems[i].date == event.travel_date){
                                listItems[i].events.add(event)
                                listItems[i].users.add(userEvent)
                                found = true
                                break
                            }
                        }
                        if(!found){
                            val newArrayEvent = ArrayList<Event>()
                            newArrayEvent.add(event)

                            val newArrayUser = ArrayList<User>()
                            newArrayUser.add(userEvent)
                            listItems.add(EventDisplay(event.travel_date, newArrayEvent, newArrayUser))
                        }
                    }
                }
                Collections.sort(listItems, EventDisplayComparator())
                adapter.notifyDataSetChanged()

                val events = ArrayList<EventDay>()
                val calendar = Calendar.getInstance()
                for(eventDisplay in listItems){
                    var date: String = eventDisplay.date

                    val year = date.substring(0, date.indexOf("-"))
                    date = date.substring(date.indexOf("-") + 1, date.lastIndex + 1)
                    val month = date.substring(0, date.indexOf("-"))
                    val day = date.substring(date.indexOf("-") + 1, date.lastIndex + 1)
                    calendar.set(year.toInt(), month.toInt() - 1, day.toInt())
                }
                events.add(EventDay(calendar, R.drawable.ic_place_primary, R.color.colorPrimary));

                val calendarView = mView.findViewById<CalendarView>(R.id.calendar)
                calendarView.setEvents(events);
                calendarView.setOnDayClickListener(object : OnDayClickListener {
                    @SuppressLint("SimpleDateFormat")
                    override fun onDayClick(eventDay: EventDay) {
                        val clickedDayCalendar = eventDay.calendar
                        val sdf = SimpleDateFormat("yyyy-MM-dd")

                        adapter.displayOnlyDate(sdf.format(clickedDayCalendar.time).toString())
                    }
                })
            }

            override fun onError(error: VolleyError): Unit {
                Log.e("EVENTS", "Event - onError")
                Log.e("CALENDRA ERROR", "Event - onError")
                Log.e("EVENTS", error.toString())
            }
        })
    }

    fun findUser(userId: Int): User?{
        for(user in group.members){
            if(user.id == userId){
                return user
            }
        }
        return null
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            initEvents()
        }
    }
}
