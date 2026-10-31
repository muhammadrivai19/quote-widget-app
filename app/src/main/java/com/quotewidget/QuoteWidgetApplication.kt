package com.quotewidget

import android.app.Application
import androidx.room.Room
import com.quotewidget.data.AppDatabase
import com.quotewidget.data.seedQuoteList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuoteWidgetApplication : Application() {
    lateinit var db: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(this, AppDatabase::class.java, "quotes.db").build()
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = getSharedPreferences("quotewidget_init", MODE_PRIVATE)
            if (!prefs.getBoolean("seed_done", false)) {
                if (db.quoteDao().count() == 0) {
                    db.quoteDao().insertAll(seedQuoteList())
                }
                prefs.edit().putBoolean("seed_done", true).apply()
            }
            com.quotewidget.widget.RotateWorker.schedule(
                this@QuoteWidgetApplication,
                com.quotewidget.data.SettingsStore.interval(this@QuoteWidgetApplication)
            )
        }
    }
}
