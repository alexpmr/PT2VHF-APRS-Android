package com.pt2vhf.aprs.core

/** #88: trecho do comentário que vira link (e-mail → mailto:, site → http/https). */
data class CommentLink(val start: Int, val end: Int, val target: String, val email: Boolean)

private val EMAIL = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}")
private val URL = Regex("(?i)\\b(?:https?://|www\\.)[^\\s<>\"]+")

/** Links do comentário, em ordem e sem sobreposição. Pontuação final (.,;:!?)) não entra no link. */
fun findCommentLinks(text: String): List<CommentLink> {
    val out = ArrayList<CommentLink>()
    EMAIL.findAll(text).forEach { m ->
        out += CommentLink(m.range.first, m.range.last + 1, "mailto:" + m.value, true)
    }
    URL.findAll(text).forEach { m ->
        var value = m.value
        while (value.isNotEmpty() && value.last() in ".,;:!?)]}'") value = value.dropLast(1)
        if (value.length < 5) return@forEach
        val start = m.range.first
        val end = start + value.length
        if (out.any { start < it.end && end > it.start }) return@forEach
        val target = if (value.startsWith("www.", ignoreCase = true)) "https://$value" else value
        out += CommentLink(start, end, target, false)
    }
    return out.sortedBy { it.start }
}
