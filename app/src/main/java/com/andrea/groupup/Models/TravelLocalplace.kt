package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*

class TravelLocalplace(var position: Int, var created_at: Date, var LocalPlaceId: Int, var TravelId: Int) : Serializable {
    override fun toString(): String {
        return "TravelLocalplace(position:$position, created_at:$created_at, LocalPlaceId:$LocalPlaceId, TravelId:$TravelId)"
    }
}