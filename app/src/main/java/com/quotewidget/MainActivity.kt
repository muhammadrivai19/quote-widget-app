package com.quotewidget

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quotewidget.data.Quote
import com.quotewidget.data.SettingsStore
import com.quotewidget.ui.OnboardingScreen
import com.quotewidget.ui.QuoteFormScreen
import com.quotewidget.ui.QuoteListScreen
import com.quotewidget.ui.QuoteViewModel
import com.quotewidget.ui.QuoteViewerScreen
import com.quotewidget.ui.QuoteWidgetTheme
import com.quotewidget.ui.SettingsScreen
import com.quotewidget.ui.quoteViewModelFactory

class MainActivity : ComponentActivity() {
    private val deepLinkIndex = mutableStateOf<Int?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkIndex.value = intent.getIntExtra(
            com.quotewidget.widget.QuoteWidgetProvider.EXTRA_VIEWER_INDEX, -1
        ).takeIf { it >= 0 }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deepLinkIndex.value = intent?.getIntExtra(
            com.quotewidget.widget.QuoteWidgetProvider.EXTRA_VIEWER_INDEX, -1
        )?.takeIf { it >= 0 }
        val app = application as QuoteWidgetApplication
        setContent {
            QuoteWidgetTheme {
                val vm: QuoteViewModel = viewModel(factory = quoteViewModelFactory(app))
                val quotes by vm.quotes.collectAsState()
                val ctx = LocalContext.current
                val scope = rememberCoroutineScope()
                val onboarded by SettingsStore.onboardingFlow(ctx).collectAsState(initial = null)
                var editing by remember { mutableStateOf<Quote?>(null) }
                var adding by remember { mutableStateOf(false) }
                var viewerIndex by remember { mutableStateOf<Int?>(null) }
                var showSettings by remember { mutableStateOf(false) }
                val interval by SettingsStore.intervalFlow(ctx).collectAsState(initial = 60)
                val order by SettingsStore.orderFlow(ctx).collectAsState(initial = SettingsStore.SEQUENTIAL)
                val themeId by SettingsStore.themeFlow(ctx).collectAsState(initial = "kertas")
                val textHex by SettingsStore.textColorFlow(ctx).collectAsState(initial = null)
                val fontId by SettingsStore.fontFlow(ctx).collectAsState(initial = "serif")
                LaunchedEffect(quotes, themeId, textHex, fontId) {
                    com.quotewidget.widget.QuoteWidgetProvider.updateAll(ctx)
                }
                LaunchedEffect(deepLinkIndex.value, quotes) {
                    val i = deepLinkIndex.value
                    if (i != null && quotes.isNotEmpty()) {
                        showSettings = false
                        adding = false
                        editing = null
                        viewerIndex = i.coerceIn(quotes.indices)
                        deepLinkIndex.value = null
                    }
                }

                when {
                    onboarded == null -> {}
                    !onboarded!! -> OnboardingScreen(onDone = {
                        scope.launch { SettingsStore.setOnboardingDone(ctx) }
                    })
                    showSettings -> SettingsScreen(
                        interval = interval,
                        order = order,
                        themeId = themeId,
                        textHex = textHex,
                        fontId = fontId,
                        onInterval = { scope.launch {
                            SettingsStore.setInterval(ctx, it)
                            com.quotewidget.widget.RotateWorker.schedule(ctx, it)
                        } },
                        onOrder = { scope.launch { SettingsStore.setOrder(ctx, it) } },
                        onTheme = { id -> scope.launch {
                            SettingsStore.setTheme(ctx, id)
                        } },
                        onTextColor = { hex -> scope.launch {
                            SettingsStore.setTextColor(ctx, hex)
                        } },
                        onFont = { id -> scope.launch {
                            SettingsStore.setFont(ctx, id)
                        } },
                        onBack = { showSettings = false }
                    )
                    adding || editing != null -> QuoteFormScreen(
                        editing = editing,
                        onSave = { text, author ->
                            var err: String? = null
                            vm.save(text, author, editing) { err = it }
                            if (err == null) { adding = false; editing = null }
                            err
                        },
                        onBack = { adding = false; editing = null }
                    )
                    viewerIndex != null -> QuoteViewerScreen(
                        quotes = quotes,
                        initialIndex = viewerIndex ?: 0,
                        themeId = themeId,
                        textHex = textHex,
                        fontId = fontId,
                        onBack = { viewerIndex = null }
                    )
                    else -> QuoteListScreen(
                        quotes = quotes,
                        onAdd = { adding = true },
                        onEdit = { editing = it },
                        onOpenViewer = { q ->
                            viewerIndex = quotes.indexOfFirst { it.id == q.id }.coerceAtLeast(0)
                        },
                        onOpenViewerAtWidget = {
                            if (quotes.isEmpty()) return@QuoteListScreen
                            val w = try {
                                com.quotewidget.data.WidgetStore.getIndex(ctx)
                            } catch (_: Exception) {
                                0
                            }
                            viewerIndex = w.coerceIn(quotes.indices)
                        },
                        onSettings = { showSettings = true },
                        onDelete = { vm.delete(it) },
                        onUndo = { vm.undoDelete() }
                    )
                }
            }
        }
    }
}
