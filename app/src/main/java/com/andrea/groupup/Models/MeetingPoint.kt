package com.andrea.groupup.Models

import android.text.Editable
import java.io.Serializable
import java.util.*

class MeetingPoint(var id: Int? = null, var description: String, var coordinate_x: String, var coordinate_y: String, var expiration_date: Date, var date_crea: Date? = null, var GroupId: Int, var UserId: Int) : Serializable {
    override fun toString(): String {
        return "Tag(id=$id, description='$description, coordinate_x:$coordinate_x, coordinate_y:$coordinate_y, expiration_date:$expiration_date, date_crea:$date_crea, GroupId:$GroupId, UserId:$UserId')"
    }
}