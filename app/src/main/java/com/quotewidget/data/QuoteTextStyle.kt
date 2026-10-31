package com.quotewidget.data

import androidx.compose.ui.text.font.FontFamily
import com.quotewidget.R

data class TextColorOption(
    val hex: String?, // null = Default tema
    val name: String,
    val argb: Long?,
    // ponytail: semua terbuka di v1; kunci saat billing jadi
    val isPremium: Boolean = false
)

val TextColorOptions = listOf(
    TextColorOption(null, "Default tema", null),
    TextColorOption("#1A1A1A", "Hitam", 0xFF1A1A1A),
    TextColorOption("#F2EFE8", "Putih tulang", 0xFFF2EFE8),
    TextColorOption("#B8A15A", "Emas", 0xFFB8A15A),
    TextColorOption("#6E6B63", "Batu", 0xFF6E6B63),
    TextColorOption("#16213A", "Navy", 0xFF16213A),
    TextColorOption("#2F4A3A", "Hijau pinus", 0xFF2F4A3A),
    TextColorOption("#6B2B3A", "Burgundy", 0xFF6B2B3A)
)

data class QuoteFont(
    val id: String,
    val name: String,
    val layout: Int,
    // ponytail: semua terbuka di v1; kunci saat billing jadi
    val isPremium: Boolean = false
)

val QuoteFonts = listOf(
    QuoteFont("sans", "Sans", R.layout.widget_quote_sans),
    QuoteFont("serif", "Serif", R.layout.widget_quote),
    QuoteFont("mono", "Mono", R.layout.widget_quote_mono),
    QuoteFont("rounded", "Rounded", R.layout.widget_quote_rounded)
)

fun fontById(id: String): QuoteFont =
    QuoteFonts.firstOrNull { it.id == id } ?: QuoteFonts.first { it.id == "serif" }

fun quoteFontFamily(id: String): FontFamily = when (id) {
    "sans" -> FontFamily.SansSerif
    "mono" -> FontFamily.Monospace
    "rounded" -> FontFamily.Cursive
    else -> FontFamily.Serif
}

fun isSansBold(id: String): Boolean = id == "sans"

fun effectiveTextArgb(theme: QuoteTheme, customHex: String?): Long {
    if (customHex == null) return theme.textColor
    return TextColorOptions.firstOrNull { it.hex == customHex }?.argb
        ?: try {
            customHex.removePrefix("#").let {
                val v = it.toLong(16)
                if (it.length == 6) 0xFF000000L or v else v
            }
        } catch (_: Exception) {
            theme.textColor
        }
}

fun derivedAuthorArgb(textArgb: Long): Int {
    val rgb = (textArgb and 0x00FFFFFF).toInt()
    return (0xA6 shl 24) or rgb
}

fun faintAccentArgb(accentArgb: Long): Int {
    val rgb = (accentArgb and 0x00FFFFFF).toInt()
    return (0x2E shl 24) or rgb
}

// WCAG contrast ratio, 1.0 - 21.0. Warn if < 4.5 for body text.
fun contrastRatio(fgArgb: Long, bgArgb: Long): Double {
    fun lum(argb: Long): Double {
        fun ch(c: Double): Double {
            val s = c / 255.0
            return if (s <= 0.03928) s / 12.92 else Math.pow((s + 0.055) / 1.055, 2.4)
        }
        val r = ch(((argb shr 16) and 0xFF).toDouble())
        val g = ch(((argb shr 8) and 0xFF).toDouble())
        val b = ch((argb and 0xFF).toDouble())
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
    val l1 = lum(fgArgb)
    val l2 = lum(bgArgb)
    val (hi, lo) = if (l1 >= l2) l1 to l2 else l2 to l1
    return (hi + 0.05) / (lo + 0.05)
}
