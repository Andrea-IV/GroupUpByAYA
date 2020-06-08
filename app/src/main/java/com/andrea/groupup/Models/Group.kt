package com.andrea.groupup.Models

import java.util.*

class Group(var id: Int, var name: String?, var date_crea: Date) {
    override fun toString(): String {
        return "Group(id=$id, name=$name, date_crea=$date_crea)"
    }
}