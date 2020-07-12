package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class LocalPlace (var id: Int, var name: String, val coordinate_x: String, val coordinate_y: String, val address: String, var description: String?, val date_crea: Date, val autorId: Int?, val GroupId: Int, var Photos: ArrayList<Photo>, var Ratings: Double?, var Tags: ArrayList<Tag>, val distance: Double?, var UserRating: Int?, var pos: Int, var TravelLocalplace: TravelLocalplace?): Serializable {
    override fun toString(): String {
        return "LocalPlace(id=$id, name='$name', coordinate_x='$coordinate_x', coordinate_y='$coordinate_y', address='$address', description='$description', date_crea=$date_crea, autorId=$autorId, GroupId:$GroupId, Photos=$Photos, Ratings=$Ratings, Tags=$Tags, distance=$distance, UserRating=$UserRating, pos=$pos, TravelLocalplace=$TravelLocalplace)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalPlace

        if (id != other.id) return false
        if (name != other.name) return false
        if (coordinate_x != other.coordinate_x) return false
        if (coordinate_y != other.coordinate_y) return false
        if (address != other.address) return false
        if (description != other.description) return false
        if (date_crea != other.date_crea) return false
        if (autorId != other.autorId) return false
        if (GroupId != other.GroupId) return false
        if (Photos != other.Photos) return false
        if (Ratings != other.Ratings) return false
        if (Tags != other.Tags) return false
        if (distance != other.distance) return false
        if (UserRating != other.UserRating) return false
        if (pos != other.pos) return false
        if (TravelLocalplace != other.TravelLocalplace) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + name.hashCode()
        result = 31 * result + coordinate_x.hashCode()
        result = 31 * result + coordinate_y.hashCode()
        result = 31 * result + address.hashCode()
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + date_crea.hashCode()
        result = 31 * result + (autorId ?: 0)
        result = 31 * result + GroupId
        result = 31 * result + Photos.hashCode()
        result = 31 * result + (Ratings?.hashCode() ?: 0)
        result = 31 * result + Tags.hashCode()
        result = 31 * result + (distance?.hashCode() ?: 0)
        result = 31 * result + (UserRating ?: 0)
        result = 31 * result + pos
        result = 31 * result + (TravelLocalplace?.hashCode() ?: 0)
        return result
    }


}