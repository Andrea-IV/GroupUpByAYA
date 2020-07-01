package com.andrea.groupup.Models

class FriendRequest(var id_user: Int, var id_friend: Int, var accepted: Int, var User: User) {
    override fun toString(): String {
        return "FriendRequest(id_user=$id_user, id_friend=$id_friend, accepted=$accepted, User=$User)"
    }
}