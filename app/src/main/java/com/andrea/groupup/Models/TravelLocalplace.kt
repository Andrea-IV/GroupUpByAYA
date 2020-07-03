package com.andrea.groupup.Models

import java.util.*

class TravelLocalplace(var position: Int, var created_at: Date, var LocalPlaceId: Int, var TravelId: Int) {
    override fun toString(): String {
        return "TravelLocalplace(position:$position, created_at:$created_at, LocalPlaceId:$LocalPlaceId, TravelId:$TravelId)"
    }
}