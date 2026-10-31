package com.quotewidget.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RotateWorker.schedule(
                    context.applicationContext,
                    com.quotewidget.data.SettingsStore.interval(context.applicationContext)
                )
                QuoteWidgetProvider.updateAll(context.applicationContext)
            } finally {
                pending.finish()
            }
        }
    }
}
