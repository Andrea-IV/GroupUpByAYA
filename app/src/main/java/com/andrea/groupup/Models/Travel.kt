package com.andrea.groupup.Models

import java.util.*
import kotlin.collections.ArrayList

class Travel(var id: Int,
             var travel_date: Date,
             var date_crea: Date,
             var UserId: Int,
             var GroupId: Int,
             var LocalPlaces: ArrayList<LocalPlace>) {

    override fun toString(): String {
        return "Travel(id=$id, travel_date:$travel_date, date_crea:$date_crea, UserId:$UserId, GroupId:$GroupId, LocalPlaces:$LocalPlaces)"
    }
}