package com.andrea.groupup.Models

import java.io.Serializable

class Tag(var id: Int, var name: String) : Serializable {
    override fun toString(): String {
        return "Tag(id=$id, name='$name')"
    }
}