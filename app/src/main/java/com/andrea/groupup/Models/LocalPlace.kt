package com.andrea.groupup.Models

import java.util.*

class LocalPlace (val id: Int, val name: String, val coordinate_x: String, val coordinate_y: String, val address: String, val opening_hour: String, val closing_hour: String, val date_crea: Date, val autorId: Int) {
    override fun toString(): String {
        return "LocalPlace(id=$id, name='$name', coordinate_x='$coordinate_x', coordinate_y='$coordinate_y', address='$address', opening_hour='$opening_hour', closing_hour='$closing_hour', date_crea=$date_crea, autorId=$autorId)"
    }
}