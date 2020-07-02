package com.andrea.groupup.Models

import java.io.Serializable

class Photo(var id: String, var link: String) : Serializable {
    override fun toString(): String {
        return "Photo(id='$id', link='$link')"
    }
}