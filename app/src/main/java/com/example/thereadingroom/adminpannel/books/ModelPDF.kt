package com.example.thereadingroom.adminpannel.books

class ModelPDF {

    var uid: String = ""
    var id: String = ""
    var title: String = ""
    var author: String = ""
    var description: String = ""
    var categoryID: String = ""
    var url: String = ""
    var timestamp: Long = 0
    var viewsCount: Long = 0
    var downloadsCount: Long = 0
    var isFavorite = false

    constructor()
    constructor(
        uid: String,
        id: String,
        title: String,
        author: String,
        description: String,
        categoryID: String,
        url: String,
        timestamp: Long,
        viewsCount: Long,
        downloadsCount: Long,
        isFavorite: Boolean
    ) {
        this.uid = uid
        this.id = id
        this.title = title
        this.author = author
        this.description = description
        this.categoryID = categoryID
        this.url = url
        this.timestamp = timestamp
        this.viewsCount = viewsCount
        this.downloadsCount = downloadsCount
        this.isFavorite = isFavorite
    }


}