package com.udyata.mangalot.data.remote

import com.udyata.mangalot.utils.extractAttribute
import com.udyata.mangalot.utils.extractText
import org.jsoup.Jsoup

data class Manga(
    val title: String,
    val url: String,
    val thumbnail: String,
    val latestChapter: String,
    val description: String,
    val views:String
)


data class MangaExtractionConfig(
    val titleSelector: String = "a.list-story-item.bookmark_check",
    val urlSelector: String = "a.list-story-item.bookmark_check",
    val thumbnailSelector: String = "img",
    val latestChapterSelector: String = "a.list-story-item-wrap-chapter",
    val descriptionSelector: String = "p",
    val viewsSelector: String = "span.aye_icon"
)


fun parseMangaList(htmlContent: String, config: MangaExtractionConfig = MangaExtractionConfig()): List<Manga> {
    val document = Jsoup.parse(htmlContent)
    val mangaList = mutableListOf<Manga>()

    document.select("div.list-truyen-item-wrap").forEach { element ->
        mangaList.add(
            Manga(
                title = element.extractAttribute(config.titleSelector, "title"),
                url = element.extractAttribute(config.urlSelector, "href"),
                thumbnail = element.extractAttribute(config.thumbnailSelector, "src"),
                latestChapter = element.extractText(config.latestChapterSelector),
                description = element.extractText(config.descriptionSelector),
                views = element.extractText(config.viewsSelector)
            )
        )
    }
    return mangaList
}