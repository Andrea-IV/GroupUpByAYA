package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Group(var id: Int, var name: String?, var date_crea: Date, var members: ArrayList<User>) : Serializable {
    override fun toString(): String {
        return "Group(id=$id, name=$name, date_crea=$date_crea, members=$members)"
    }
}