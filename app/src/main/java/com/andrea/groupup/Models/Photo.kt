package com.andrea.groupup.Models

import java.io.Serializable

class Photo(var id: String, var link: String, var allow_share: Boolean, var UserId: String) : Serializable {
    override fun toString(): String {
        return "Photo(id='$id', link='$link', allow_share=$allow_share)"
    }
}