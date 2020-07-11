package com.andrea.groupup.Models

import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class LocalPlace (var id: Int, val name: String, val coordinate_x: String, val coordinate_y: String, val address: String, val description: String?, val date_crea: Date, val autorId: Int?, val GroupId: Int, var Photos: ArrayList<Photo>, val Ratings: Double?, var Tags: ArrayList<Tag>, val distance: Double?, var UserRating: Int?, var pos: Int, var TravelLocalplace: TravelLocalplace?): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LocalPlace

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "LocalPlace(id=$id, name='$name', coordinate_x='$coordinate_x', coordinate_y='$coordinate_y', address='$address', description='$description', date_crea=$date_crea, autorId=$autorId, GroupId:$GroupId, Photos=$Photos, Ratings=$Ratings, Tags=$Tags, distance=$distance, UserRating=$UserRating, pos=$pos, TravelLocalplace=$TravelLocalplace)"
    }

}