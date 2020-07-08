package com.andrea.groupup.Models

class LocalPlaceComparator : Comparator<LocalPlace> {

    override fun compare(o1: LocalPlace?, o2: LocalPlace?): Int {
        if (o1 != null && o2 != null) {
            return o1.pos.compareTo(o2.pos)
        }
        return 0
    }
}