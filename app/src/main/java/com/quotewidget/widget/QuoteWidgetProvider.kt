package com.quotewidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.quotewidget.MainActivity
import com.quotewidget.R
import com.quotewidget.data.WidgetStore
import com.quotewidget.data.contrastRatio
import com.quotewidget.data.derivedAuthorArgb
import com.quotewidget.data.effectiveTextArgb
import com.quotewidget.data.faintAccentArgb
import com.quotewidget.data.fontById
import com.quotewidget.data.themeById
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) updateOne(context, mgr, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STEP) {
            // ponytail: goAsync keeps receiver alive; fire-and-forget launch can die with process
            val pending = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val mgr = AppWidgetManager.getInstance(context)
                    val delta = intent.getIntExtra(EXTRA_DELTA, 1)
                    val quotes = loadQuotes(context)
                    if (quotes.isNotEmpty()) WidgetStore.step(context, quotes.size, delta)
                    val ids = mgr.getAppWidgetIds(
                        android.content.ComponentName(context, QuoteWidgetProvider::class.java)
                    )
                    for (id in ids) updateOneSync(context, mgr, id)
                } catch (e: Exception) {
                    android.util.Log.e("QuoteWidget", "step fail", e)
                } finally {
                    pending.finish()
                }
            }
        } else {
            super.onReceive(context, intent)
        }
    }

    companion object {
        const val ACTION_STEP = "com.quotewidget.WIDGET_STEP"
        const val EXTRA_DELTA = "delta"
        const val EXTRA_VIEWER_INDEX = "viewerIndex"

        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(
                android.content.ComponentName(context, QuoteWidgetProvider::class.java)
            )
            for (id in ids) updateOne(context, mgr, id)
        }

        internal fun updateOne(context: Context, mgr: AppWidgetManager, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateOneSync(context, mgr, id)
                } catch (e: Exception) {
                    android.util.Log.e("QuoteWidget", "update fail", e)
                }
            }
        }

        internal suspend fun updateOneSync(context: Context, mgr: AppWidgetManager, id: Int) {
            val quotes = loadQuotes(context)
            val font = try {
                fontById(com.quotewidget.data.SettingsStore.font(context))
            } catch (_: Exception) {
                fontById("serif")
            }
            val rv = RemoteViews(context.packageName, font.layout)
            applyTheme(context, rv)
            var currentIdx = 0
                if (quotes.isEmpty()) {
                    rv.setTextViewText(R.id.tv_quote, "Tambah quote di app")
                    rv.setTextViewText(R.id.tv_author, "")
                    rv.setViewVisibility(R.id.tv_author, View.INVISIBLE)
                    rv.setViewVisibility(R.id.tv_mark, View.VISIBLE)
                    rv.setTextViewText(R.id.tv_mark, "“")
                    rv.setViewVisibility(R.id.btn_prev, View.GONE)
                    rv.setViewVisibility(R.id.btn_next, View.GONE)
                } else {
                    val idx = WidgetStore.getIndex(context).coerceIn(quotes.indices)
                    currentIdx = idx
                    val q = quotes[idx]
                    rv.setViewVisibility(R.id.tv_mark, View.VISIBLE)
                    rv.setTextViewText(R.id.tv_mark, "“")
                    rv.setTextViewText(R.id.tv_quote, q.text)
                    // ponytail: shrink manual per panjang, RemoteViews autosize tak andal
                    val sizeSp = when {
                        q.text.length > 180 -> 13f
                        q.text.length > 120 -> 14f
                        q.text.length > 60 -> 16f
                        q.text.length > 30 -> 18f
                        else -> 20f
                    }
                    try {
                        rv.setTextViewTextSize(R.id.tv_quote, android.util.TypedValue.COMPLEX_UNIT_SP, sizeSp)
                    } catch (_: Exception) {
                    }
                    val author = q.author?.trim().orEmpty()
                    rv.setTextViewText(R.id.tv_author, author.uppercase())
                    // ponytail: INVISIBLE keeps slot, tv_quote stays centered with/without author
                    rv.setViewVisibility(R.id.tv_author, if (author.isEmpty()) View.INVISIBLE else View.VISIBLE)
                    val single = quotes.size == 1
                    rv.setViewVisibility(R.id.btn_prev, if (single) View.GONE else View.VISIBLE)
                    rv.setViewVisibility(R.id.btn_next, if (single) View.GONE else View.VISIBLE)
                    rv.setOnClickPendingIntent(R.id.btn_prev, stepIntent(context, -1))
                    rv.setOnClickPendingIntent(R.id.btn_next, stepIntent(context, 1))
                }
                val openPi = openAppIntent(context, currentIdx, id)
                rv.setOnClickPendingIntent(R.id.quote_body, openPi)
                rv.setOnClickPendingIntent(R.id.tv_quote, openPi)
                rv.setOnClickPendingIntent(R.id.tv_mark, openPi)
                rv.setOnClickPendingIntent(R.id.tv_author, openPi)
                // ponytail: no click on widget_root — parent handler shadows chevron buttons
                try {
                    mgr.updateAppWidget(id, rv)
                } catch (_: Exception) {
                }
        }

        private fun stepIntent(context: Context, delta: Int): PendingIntent {
            val i = Intent(context, QuoteWidgetProvider::class.java).apply {
                action = ACTION_STEP
                putExtra(EXTRA_DELTA, delta)
                data = android.net.Uri.parse("quotewidget://step/$delta")
            }
            val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getBroadcast(context, 100 + delta, i, flag)
        }

        private fun openAppIntent(context: Context, index: Int, widgetId: Int): PendingIntent {
            val i = Intent(context, MainActivity::class.java).apply {
                action = "com.quotewidget.OPEN_VIEWER"
                putExtra(EXTRA_VIEWER_INDEX, index)
            }
            val flag = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            return PendingIntent.getActivity(context, 1000 + widgetId, i, flag)
        }

        internal suspend fun loadQuotes(context: Context) =
            try {
                val app = context.applicationContext as com.quotewidget.QuoteWidgetApplication
                app.db.quoteDao().getAll()
            } catch (_: Exception) {
                emptyList()
            }

        private suspend fun applyTheme(context: Context, rv: RemoteViews) {
            val t = try {
                themeById(com.quotewidget.data.SettingsStore.theme(context))
            } catch (_: Exception) {
                themeById("kertas")
            }
            val customHex = try {
                com.quotewidget.data.SettingsStore.textColor(context)
            } catch (_: Exception) {
                null
            }
            val textArgb = effectiveTextArgb(t, customHex)
            try {
                rv.setInt(R.id.widget_root, "setBackgroundResource", t.bgDrawable)
            } catch (e: Exception) {
                android.util.Log.e("QuoteWidget", "bgResource fail ${t.id}", e)
                try {
                    rv.setInt(R.id.widget_root, "setBackgroundColor", t.bgColor.toInt())
                } catch (_: Exception) {
                }
            }
            rv.setTextColor(R.id.tv_quote, textArgb.toInt())
            rv.setTextColor(R.id.tv_author, derivedAuthorArgb(textArgb))
            rv.setTextColor(R.id.tv_mark, faintAccentArgb(t.accentColor))
            try {
                rv.setInt(R.id.btn_prev, "setColorFilter", t.chevronColor.toInt())
                rv.setInt(R.id.btn_next, "setColorFilter", t.chevronColor.toInt())
            } catch (_: Exception) {
            }
        }
    }
}
