package com.quotewidget.data

import android.content.Context

object WidgetStore {
    private const val PREFS = "quote_widget"
    private const val KEY_INDEX = "index"

    fun getIndex(context: Context): Int =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getInt(KEY_INDEX, 0)

    fun setIndex(context: Context, index: Int) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_INDEX, index).apply()
    }

    fun step(context: Context, size: Int, delta: Int): Int {
        if (size <= 0) return 0
        val cur = getIndex(context).coerceIn(0, size - 1)
        val next = (cur + delta).mod(size)
        setIndex(context, next)
        return next
    }
}
