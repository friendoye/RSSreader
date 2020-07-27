package com.friendoye.rss_reader

import com.friendoye.rss_reader.model.RssFeedItem
import com.j256.ormlite.field.DatabaseField
import java.util.*

class TestFeedItem(
    id: Int,
    title: String,
    link: String,
    source: String,
    publicationDate: Date = Date(),
    imageUrl: String? = null
) : RssFeedItem() {
    init {
        super.id = id
        super.title = title
        super.link = link
        super.source = source
        super.publicationDate = publicationDate
        super.imageUrl = imageUrl
    }
}
