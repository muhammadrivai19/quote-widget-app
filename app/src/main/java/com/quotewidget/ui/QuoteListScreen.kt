package com.quotewidget.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quotewidget.R
import com.quotewidget.data.Quote
import com.quotewidget.data.SettingsStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun QuoteListScreen(
    quotes: List<Quote>,
    onAdd: () -> Unit,
    onEdit: (Quote) -> Unit,
    onOpenViewer: (Quote) -> Unit,
    onOpenViewerAtWidget: () -> Unit,
    onSettings: () -> Unit,
    onDelete: (Quote) -> Unit,
    onUndo: () -> Unit,
    snackbar: SnackbarHostState = remember { SnackbarHostState() }
) {
    val dark = isSystemInDarkTheme()
    val bg = if (dark) Color(0xFF111111) else Color(0xFFFFFFFF)
    val text = if (dark) Color(0xFFF2EFE8) else Color(0xFF111111)
    val muted = if (dark) Color(0xFF85837C) else Color(0xFF8A8A85)
    val divider = if (dark) Color(0xFF34342F) else Color(0xFFE0E0DB)
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val ctx = LocalContext.current
    val hintShown by SettingsStore.swipeHintShownFlow(ctx).collectAsState(initial = true)
    var hintVisible by remember { mutableStateOf(false) }
    LaunchedEffect(hintShown, quotes.isNotEmpty()) {
        if (!hintShown && quotes.isNotEmpty() && !hintVisible) {
            hintVisible = true
            SettingsStore.setSwipeHintShown(ctx)
        }
    }
    // ponytail: no BackHandler here, system back exits from list (root)
    // ponytail: peek flag per item, no vm change; once via DataStore

    Scaffold(
        containerColor = bg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAdd,
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                containerColor = if (dark) Color(0xFFF2EFE8) else Color(0xFF111111),
                contentColor = if (dark) Color(0xFF111111) else Color(0xFFFFFFFF)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(20.dp))
            }
        },
        // ponytail: Scaffold snackbarHost lifts FAB above Snackbar, no overlap
        snackbarHost = { SnackbarHost(snackbar) }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Quotes",
                        fontFamily = FontFamily.Serif,
                        fontSize = 30.sp,
                        lineHeight = 34.sp,
                        color = text
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${quotes.size} tersimpan",
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 12.sp,
                        color = muted
                    )
                }
                IconButton(
                    onClick = onOpenViewerAtWidget,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painterResource(R.drawable.ic_viewer_expand),
                        contentDescription = "Buka Quote Viewer",
                        tint = text,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Buka Pengaturan", tint = text)
                }
            }
            if (hintVisible) {
                Text(
                    "Geser kiri untuk menghapus",
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                    color = muted,
                    modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 8.dp)
                )
                LaunchedEffect(Unit) {
                    delay(8000)
                    hintVisible = false
                }
            }
            if (quotes.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Belum ada quote",
                            fontFamily = FontFamily.Serif,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            color = text
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tambahkan quote pertamamu",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = muted
                        )
                        Spacer(Modifier.height(12.dp))
                        TextButton(onClick = onAdd) {
                            Text("Tambah quote", fontFamily = FontFamily.SansSerif, color = text)
                        }
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp)
                ) {
                    itemsIndexed(quotes, key = { _, q -> q.id }) { index, q ->
                        val state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { v ->
                                if (v == SwipeToDismissBoxValue.EndToStart) {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onDelete(q)
                                    scope.launch {
                                        val r = snackbar.showSnackbar("Quote dihapus", actionLabel = "Urungkan")
                                        if (r == SnackbarResult.ActionPerformed) onUndo()
                                    }
                                    true
                                } else false
                            }
                        )
                        SwipeToDismissBox(
                            state = state,
                            // ponytail: placement anim, no new dep (animateItem needs foundation 1.7+)
                            modifier = Modifier.animateItemPlacement(),
                            enableDismissFromStartToEnd = false,
                            enableDismissFromEndToStart = true,
                            backgroundContent = {
                                Box(
                                    Modifier.fillMaxSize().background(bg).padding(vertical = 14.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = muted,
                                        modifier = Modifier.padding(end = 4.dp)
                                    )
                                }
                            }
                        ) {
                            Column(Modifier.fillMaxWidth().background(bg)) {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onEdit(q) },
                                            onLongClick = { onOpenViewer(q) }
                                        )
                                        .padding(vertical = 14.dp)
                                ) {
                                    Text(
                                        "%02d".format(index + 1),
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 12.sp,
                                        color = muted,
                                        modifier = Modifier.padding(top = 3.dp).width(32.dp)
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            q.text,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 16.sp,
                                            lineHeight = 21.6.sp,
                                            color = text
                                        )
                                        val author = q.author?.trim().orEmpty()
                                        if (author.isNotEmpty()) {
                                            Spacer(Modifier.height(6.dp))
                                            Text(
                                                author.uppercase(),
                                                fontFamily = FontFamily.SansSerif,
                                                fontSize = 11.sp,
                                                letterSpacing = 1.2.sp,
                                                color = muted
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = divider)
                            }
                        }
                    }
                }
            }
        }
    }
}
