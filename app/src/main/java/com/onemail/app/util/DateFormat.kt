package com.onemail.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateFormat {

    private val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss"
    )

    fun parse(iso: String?): Date? {
        if (iso.isNullOrBlank()) return null
        for (pattern in patterns) {
            try {
                val format = SimpleDateFormat(pattern, Locale.US)
                if (pattern.endsWith("'Z'")) {
                    format.timeZone = TimeZone.getTimeZone("UTC")
                }
                val date = format.parse(iso)
                if (date != null) return date
            } catch (_: Exception) {
                // пробуем следующий формат
            }
        }
        return null
    }

    fun shortTime(iso: String?): String {
        val date = parse(iso) ?: return ""
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply { time = date }
        val sameDay = now.get(Calendar.YEAR) == then.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR)
        return if (sameDay) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        } else {
            SimpleDateFormat("d MMM", Locale("ru")).format(date)
        }
    }

    fun fullTime(iso: String?): String {
        val date = parse(iso) ?: return ""
        return SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("ru")).format(date)
    }
}
