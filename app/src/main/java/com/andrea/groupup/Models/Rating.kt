package com.andrea.groupup.Models

import java.io.Serializable

class Rating(val id: Int, val rating: Int, val LocalPlaceId: Int, val UserId: Int) : Serializable{
    override fun toString(): String {
        return "Rating(id=$id, rating=$rating, LocalPlaceId=$LocalPlaceId, UserId=$UserId)"
    }
}