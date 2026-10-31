package com.quotewidget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotewidget.data.QuoteFonts
import com.quotewidget.data.QuoteTheme
import com.quotewidget.data.QuoteThemes
import com.quotewidget.data.SettingsStore
import com.quotewidget.data.TextColorOptions
import com.quotewidget.data.contrastRatio
import com.quotewidget.data.effectiveTextArgb
import com.quotewidget.data.isSansBold
import com.quotewidget.data.quoteFontFamily
import com.quotewidget.data.themeById

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    interval: Int,
    order: String,
    themeId: String,
    textHex: String?,
    fontId: String,
    onInterval: (Int) -> Unit,
    onOrder: (String) -> Unit,
    onTheme: (String) -> Unit,
    onTextColor: (String?) -> Unit,
    onFont: (String) -> Unit,
    onBack: () -> Unit
) {
    val theme = themeById(themeId)
    val eff = effectiveTextArgb(theme, textHex)
    val textColor = Color(eff)
    val accentColor = Color(theme.accentColor)

    BackHandler(onBack = onBack)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(theme.bgColor),
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor
                )
            )
        },
        containerColor = Color(theme.bgColor)
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {
            Text("Interval pergantian", color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            SettingsStore.INTERVALS.forEach { m ->
                Row(
                    Modifier.fillMaxWidth().clickable { onInterval(m) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = m == interval, 
                        onClick = { onInterval(m) },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(label(m), color = textColor)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Urutan quote", color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clickable { onOrder(SettingsStore.SEQUENTIAL) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = order == SettingsStore.SEQUENTIAL, 
                    onClick = { onOrder(SettingsStore.SEQUENTIAL) },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = accentColor)
                )
                Spacer(Modifier.width(8.dp))
                Text("Berurutan", color = textColor)
            }
            Row(
                Modifier.fillMaxWidth().clickable { onOrder(SettingsStore.RANDOM) }.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = order == SettingsStore.RANDOM, 
                    onClick = { onOrder(SettingsStore.RANDOM) },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = accentColor)
                )
                Spacer(Modifier.width(8.dp))
                Text("Acak", color = textColor)
            }
            Spacer(Modifier.height(16.dp))
            Text("Tema (widget + viewer)", color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            QuoteThemes.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth()) {
                    row.forEach { t ->
                        Box(Modifier.weight(1f)) {
                            ThemeCard(t, selected = t.id == themeId || legacyMatch(t, themeId)) {
                                onTheme(t.id)
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Warna teks", color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth().clickable { onTextColor(null) }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = textHex == null, 
                    onClick = { onTextColor(null) },
                    colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = accentColor)
                )
                Spacer(Modifier.width(8.dp))
                Text("Default tema", color = textColor)
            }
            TextColorOptions.filter { it.hex != null }.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    row.forEach { opt ->
                        val c = Color(opt.argb!!)
                        val sel = opt.hex == textHex
                        Column(
                            Modifier.weight(1f).clickable { onTextColor(opt.hex) },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        if (sel) 3.dp else 1.dp,
                                        if (sel) accentColor else Color.Gray.copy(alpha = 0.5f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sel) Icon(Icons.Default.Check, contentDescription = "Dipilih", tint = if (contrastRatio(opt.argb, 0xFFFFFFFF) > 2.5) Color.White else Color.Black)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(opt.name, color = textColor, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                        }
                    }
                    repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
            Spacer(Modifier.height(4.dp))
            val ratio = contrastRatio(eff, theme.bgColor)
            Text(
                "Kontras %.1f:1".format(ratio),
                color = textColor.copy(alpha = 0.7f),
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            if (ratio < 4.5) {
                Spacer(Modifier.height(2.dp))
                Text(
                    "Kontras rendah, teks sulit dibaca di tema ini.",
                    color = Color(0xFFB00020),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                )
            }
            Spacer(Modifier.height(16.dp))
            Text("Font", color = textColor, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            QuoteFonts.forEach { f ->
                val sel = f.id == fontId
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (sel) accentColor.copy(alpha = 0.15f) else Color.Transparent)
                        .border(
                            if (sel) 2.dp else 1.dp,
                            if (sel) accentColor else Color.Gray.copy(alpha = 0.3f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onFont(f.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = sel, 
                        onClick = { onFont(f.id) },
                        colors = androidx.compose.material3.RadioButtonDefaults.colors(selectedColor = accentColor)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(f.name, color = textColor, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "Hidup itu 10% apa yang terjadi padamu.",
                            fontFamily = quoteFontFamily(f.id),
                            fontWeight = if (isSansBold(f.id)) FontWeight.Bold else null,
                            fontSize = 15.sp,
                            color = textColor
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Android hemat baterai, update bisa meleset beberapa menit. Normal.", color = textColor.copy(alpha = 0.6f), style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ThemeCard(t: QuoteTheme, selected: Boolean, onPick: () -> Unit) {
    Column(
        Modifier.padding(6.dp).clickable { onPick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(t.bgColor))
                .border(
                    if (selected) 2.dp else 1.dp,
                    if (selected) Color(t.accentColor) else Color.Gray.copy(alpha = 0.4f),
                    RoundedCornerShape(20.dp)
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("“", fontFamily = FontFamily.Serif, fontSize = 18.sp, color = Color(t.accentColor))
                Text(
                    "Hidup itu 10% apa yang terjadi padamu.",
                    fontFamily = FontFamily.Serif,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color(t.textColor),
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "CHARLES SWINDOLL",
                    fontFamily = FontFamily.Serif,
                    fontSize = 8.sp,
                    letterSpacing = 1.sp,
                    color = Color(t.authorColor)
                )
            }
            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Dipilih",
                    tint = Color(t.accentColor),
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(t.name)
    }
}

private fun legacyMatch(t: QuoteTheme, saved: String): Boolean =
    saved == "light" && t.id == "kertas" ||
        saved == "dark" && t.id == "tinta" ||
        saved == "blue" && t.id == "batu" ||
        saved == "sunset" && t.id == "kertas"

private fun label(m: Int): String = when {
    m < 60 -> "$m menit"
    m == 60 -> "1 jam"
    m < 1440 -> "${m / 60} jam"
    else -> "24 jam"
}
