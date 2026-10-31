package com.quotewidget.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotewidget.data.Quote

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun QuoteViewerScreen(
    quotes: List<Quote>,
    initialIndex: Int,
    themeId: String,
    textHex: String?,
    fontId: String,
    onBack: () -> Unit
) {
    if (quotes.isEmpty()) {
        BackHandler(onBack = onBack)
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Belum ada quote.")
        }
        return
    }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(quotes.indices),
        pageCount = { quotes.size }
    )
    var clean by remember { mutableStateOf(false) }
    BackHandler { onBack() }
    val theme = com.quotewidget.data.themeById(themeId)
    val textArgb = com.quotewidget.data.effectiveTextArgb(theme, textHex)
    val textColor = Color(textArgb)
    val authorColor = Color(com.quotewidget.data.derivedAuthorArgb(textArgb))
    val markColor = Color(com.quotewidget.data.faintAccentArgb(theme.accentColor))
    val ff = com.quotewidget.data.quoteFontFamily(fontId)
    val quoteWeight = if (com.quotewidget.data.isSansBold(fontId)) androidx.compose.ui.text.font.FontWeight.Bold else null

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(theme.bgColor))
    ) {
        if (!clean) {
            TopAppBar(
                title = { Text("${pagerState.currentPage + 1} / ${quotes.size}") },
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
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { clean = !clean }
        ) { page ->
            val q = quotes[page]
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                // ponytail: watermark faint di belakang, tak geser posisi teks
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "“",
                        fontFamily = ff,
                        fontSize = 120.sp,
                        lineHeight = 120.sp,
                        textAlign = TextAlign.Center,
                        color = markColor
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // ponytail: shrink manual per panjang, bukan AutoSize (butuh lib baru)
                        val fontSize = when {
                            q.text.length > 150 -> 18.sp
                            q.text.length > 100 -> 20.sp
                            q.text.length > 60 -> 22.sp
                            else -> 26.sp
                        }
                        Text(
                            q.text,
                            fontFamily = ff,
                            fontWeight = quoteWeight,
                            fontSize = fontSize,
                            lineHeight = 34.sp,
                            textAlign = TextAlign.Center,
                            color = textColor,
                            maxLines = 12,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        // ponytail: slot author fixed 28dp, teks tetap center ada/tanpa author
                        val author = q.author?.trim().orEmpty()
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier.height(28.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (author.isNotEmpty()) {
                                Text(
                                    author.uppercase(),
                                    fontFamily = ff,
                                    fontSize = 12.sp,
                                    letterSpacing = 1.5.sp,
                                    textAlign = TextAlign.Center,
                                    color = authorColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
