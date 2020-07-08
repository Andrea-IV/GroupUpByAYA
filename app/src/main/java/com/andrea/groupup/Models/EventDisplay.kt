package com.andrea.groupup.Models

import java.io.Serializable
import kotlin.collections.ArrayList

class EventDisplay(var date: String, var events: ArrayList<Event>, var users: ArrayList<User>) : Serializable {
    override fun toString(): String {
        return "EventDisplay(date=$date, events=$events, users=$users)"
    }
}