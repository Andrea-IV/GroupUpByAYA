package com.andrea.groupup.Models

import java.io.Serializable

class Translation(var id: String, var title: String, var content: String) : Serializable {
    override fun toString(): String {
        return "Translation(id='$id', title='$title', content='$content')"
    }
}