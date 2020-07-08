package com.andrea.groupup.Models

class EventDisplayComparator : Comparator<EventDisplay> {

    override fun compare(o1: EventDisplay?, o2: EventDisplay?): Int {
        if (o1 != null && o2 != null) {
            return o1.date.compareTo(o2.date)
        }
        return 0
    }

}