package com.andrea.groupup

class Group {
    var id: Int? = null
    var title: String? = null
    var image: Int = 0

    constructor(id: Int?, title: String?, image: Int) {
        this.id = id
        this.title = title
        this.image = image
    }
}