package com.andrea.groupup.Models

import java.io.Serializable

class NotificationMessage (var title: String, var body: String, var tag: String?): Serializable {
    override fun toString(): String {
        return "NotificationMessage:(title:$title, body:$body, tag:$tag)"
    }
}