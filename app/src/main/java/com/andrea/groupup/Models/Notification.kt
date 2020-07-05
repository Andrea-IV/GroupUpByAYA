package com.andrea.groupup.Models

import java.io.Serializable

class Notification (var groupId: Int, var message: NotificationMessage): Serializable {
    override fun toString(): String {
        return "Notification(groupId:$groupId, message:$message)"
    }
}