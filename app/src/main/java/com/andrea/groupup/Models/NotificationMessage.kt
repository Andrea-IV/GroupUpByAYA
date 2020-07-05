package com.andrea.groupup.Models

import java.io.Serializable

class NotificationMessage (var notification: Notif): Serializable {
    override fun toString(): String {
        return "NotificationMessage:(notification:$notification)"
    }
}