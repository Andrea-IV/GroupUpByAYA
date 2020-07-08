package com.andrea.groupup.Models

class Notif (var title: String, var body: String, var tag: String?) {
    override fun toString(): String {
        return "Notif(title:$title, body:$body, tag:$tag)"
    }
}