package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Group(var id: Int, var name: String?, var picture: String?, var date_crea: Date, var members: ArrayList<User>) : Serializable {
    override fun toString(): String {
        return "Group(id=$id, name=$name, picture=$picture, date_crea=$date_crea, members=$members)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Group

        if (id != other.id) return false
        if (name != other.name) return false
        if (picture != other.picture) return false
        if (date_crea != other.date_crea) return false
        if (members != other.members) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + (name?.hashCode() ?: 0)
        result = 31 * result + (picture?.hashCode() ?: 0)
        result = 31 * result + date_crea.hashCode()
        result = 31 * result + members.hashCode()
        return result
    }


}