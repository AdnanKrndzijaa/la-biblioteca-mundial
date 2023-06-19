package com.example.thereadingroom

class ModelRecommendation {
    var id = ""
    var bookID = ""
    var timestamp = ""
    var comment = ""
    var uid = ""

    constructor()

    constructor(
        id: String,
        bookID: String,
        timestamp: String,
        comment: String,
        uid: String
    ) {
        this.id = id
        this.bookID = bookID
        this.timestamp = timestamp
        this.comment = comment
        this.uid = uid
    }
}