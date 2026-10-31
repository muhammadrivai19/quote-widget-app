package com.quotewidget.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.quotewidget.QuoteWidgetApplication

fun quoteViewModelFactory(app: QuoteWidgetApplication) = viewModelFactory {
    initializer {
        QuoteViewModel(app.db.quoteDao())
    }
}

class QuoteViewModelProvider(private val app: QuoteWidgetApplication) {
    fun factory(): ViewModelProvider.Factory = quoteViewModelFactory(app)
}
