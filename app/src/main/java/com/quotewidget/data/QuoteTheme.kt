package com.quotewidget.data

import com.quotewidget.R

data class QuoteTheme(
    val id: String,
    val name: String,
    val bgDrawable: Int,
    val bgColor: Long,
    val textColor: Long,
    val authorColor: Long,
    val accentColor: Long,
    val chevronColor: Long,
    // ponytail: semua terbuka di v1; kunci saat billing jadi
    val isPremium: Boolean = false
)

val QuoteThemes = listOf(
    QuoteTheme("kertas", "Kertas", R.drawable.widget_bg_kertas, 0xFFF7F5F0, 0xFF1A1A1A, 0xFF7A7568, 0xFFB8A15A, 0xFFC9C4B6),
    QuoteTheme("tinta", "Tinta", R.drawable.widget_bg_tinta, 0xFF111111, 0xFFF2EFE8, 0xFF8F8B80, 0xFFB8A15A, 0xFF4A4842),
    QuoteTheme("batu", "Batu", R.drawable.widget_bg_batu, 0xFFE4E2DD, 0xFF2B2B2B, 0xFF6E6B63, 0xFF6E6B63, 0xFFA9A69D)
)

private val legacyThemeMap = mapOf("light" to "kertas", "dark" to "tinta", "blue" to "batu", "sunset" to "kertas")

fun themeById(id: String): QuoteTheme =
    QuoteThemes.firstOrNull { it.id == id }
        ?: QuoteThemes.firstOrNull { it.id == legacyThemeMap[id] }
        ?: QuoteThemes[0]
