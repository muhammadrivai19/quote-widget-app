package com.quotewidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.quotewidget.data.SettingsStore
import com.quotewidget.data.WidgetStore
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RotateWorker(app: Context, p: WorkerParameters) : CoroutineWorker(app, p) {
    override suspend fun doWork(): Result {
        rotate(applicationContext)
        return Result.success()
    }

    companion object {
        private const val NAME = "quote-rotate"

        fun schedule(context: Context, minutes: Int) {
            val req = PeriodicWorkRequestBuilder<RotateWorker>(
                minutes.coerceAtLeast(15).toLong(), TimeUnit.MINUTES
            ).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NAME, ExistingPeriodicWorkPolicy.UPDATE, req
            )
        }

        suspend fun rotate(context: Context) {
            val app = context.applicationContext as? com.quotewidget.QuoteWidgetApplication
                ?: return
            val quotes = try {
                app.db.quoteDao().getAll()
            } catch (_: Exception) {
                return
            }
            if (quotes.isEmpty()) return
            val idx = if (SettingsStore.order(context) == SettingsStore.RANDOM) {
                val cur = WidgetStore.getIndex(context).coerceIn(quotes.indices)
                var n = Random.nextInt(quotes.size)
                if (quotes.size > 1) while (n == cur) n = Random.nextInt(quotes.size)
                n
            } else {
                WidgetStore.step(context, quotes.size, 1)
            }
            WidgetStore.setIndex(context, idx)
            val mgr = AppWidgetManager.getInstance(context)
            val ids = mgr.getAppWidgetIds(ComponentName(context, QuoteWidgetProvider::class.java))
            for (id in ids) QuoteWidgetProvider.updateOne(context, mgr, id)
        }
    }
}
