package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*

class User(var id: Int, var firstname: String, var lastname: String, var username: String, var pp_link: String, var email: String, var date_insc: Date, var admin: Int, var enabled: Int) :
    Serializable {
    override fun toString(): String {
        return "User(id=$id, firstname='$firstname', lastname='$lastname', username='$username', pp_link='$pp_link', email='$email', date_insc=$date_insc, admin=$admin, enabled=$enabled)"
    }
}