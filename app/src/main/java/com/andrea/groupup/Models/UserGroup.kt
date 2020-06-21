package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*

class UserGroup(var is_admin: Boolean, var coordinate_x: String, var coordinate_y: String, var is_sharing_pos: Boolean, var created_at: Date, var GroupId: Int, var UserId: Int) : Serializable {
    override fun toString(): String {
        return "UserGroup(is_admin=$is_admin, coordinate_x='$coordinate_x', coordinate_y='$coordinate_y', is_sharing_pos=$is_sharing_pos, created_at=$created_at, GroupId=$GroupId, UserId=$UserId)"
    }
}