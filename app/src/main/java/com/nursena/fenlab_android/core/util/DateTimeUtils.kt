package com.nursena.fenlab_android.core.util

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object DateTimeUtils {

    private val displayFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val fullFormatter    = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
    private val isoFormatter     = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    // "2024-01-15T10:30:00" → LocalDateTime
    fun parse(dateString: String?): LocalDateTime? = try {
        dateString?.let { LocalDateTime.parse(it, isoFormatter) }
    } catch (e: Exception) { null }

    // LocalDateTime → "15 Oca 2024"
    fun toDisplayDate(dateString: String?): String {
        val dt = parse(dateString) ?: return ""
        return dt.format(displayFormatter)
    }

    // LocalDateTime → "15.01.2024 10:30"
    fun toFullDisplay(dateString: String?): String {
        val dt = parse(dateString) ?: return ""
        return dt.format(fullFormatter)
    }

    // "3 dakika önce", "2 saat önce", "5 gün önce" gibi gösterim
    fun toRelative(dateString: String?): String {
        val dt = parse(dateString) ?: return ""
        val now = LocalDateTime.now()

        val minutes = ChronoUnit.MINUTES.between(dt, now)
        val hours   = ChronoUnit.HOURS.between(dt, now)
        val days    = ChronoUnit.DAYS.between(dt, now)
        val weeks   = ChronoUnit.WEEKS.between(dt, now)
        val months  = ChronoUnit.MONTHS.between(dt, now)

        return when {
            minutes < 1   -> "Az önce"
            minutes < 60  -> "$minutes dakika önce"
            hours   < 24  -> "$hours saat önce"
            days    < 7   -> "$days gün önce"
            weeks   < 4   -> "$weeks hafta önce"
            months  < 12  -> "$months ay önce"
            else          -> toDisplayDate(dateString)
        }
    }
}