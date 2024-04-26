package com.udyata.mangalot.utils

import org.jsoup.nodes.Element

fun Element.extractAttribute(selector: String, attribute: String): String =
    select(selector).attr(attribute)

fun Element.extractText(selector: String): String =
    select(selector).text()

fun Element.extractText(selector: String, attr: String = ""): String {
    return if (attr.isEmpty()) {
        select(selector).text().trim()
    } else {
        select(selector).attr(attr).trim()
    }
}
