package com.andrea.groupup.Models

import java.io.Serializable

class Event(var id: Int, var travel_date: String, var travel_date_original: String, var UserId: Int, var GroupId: Int, var LocalPlaces: ArrayList<LocalPlace>) : Serializable {
    override fun toString(): String {
        return "Event(id=$id, travel_date='$travel_date', travel_date_original='$travel_date_original', UserId=$UserId, GroupId=$GroupId, LocalPlaces=$LocalPlaces)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Event

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }


}