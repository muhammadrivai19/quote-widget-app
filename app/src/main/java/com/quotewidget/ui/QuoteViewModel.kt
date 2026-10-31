package com.quotewidget.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotewidget.data.Quote
import com.quotewidget.data.QuoteDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuoteViewModel(private val dao: QuoteDao) : ViewModel() {
    val quotes = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var lastDeleted: Quote? = null
        private set

    fun save(text: String, author: String?, editing: Quote?, onError: (String) -> Unit) {
        val t = text.trim()
        if (t.isEmpty()) { onError("Teks quote wajib diisi"); return }
        if (t.length > 200) { onError("Maks 200 karakter"); return }
        val a = author?.trim().orEmpty().ifEmpty { null }
        viewModelScope.launch {
            if (editing == null) dao.insert(Quote(text = t, author = a))
            else dao.update(editing.copy(text = t, author = a))
        }
    }

    fun delete(q: Quote) {
        lastDeleted = q
        viewModelScope.launch { dao.delete(q) }
    }

    fun undoDelete() {
        val q = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            // ponytail: keep id+createdAt so ORDER BY createdAt DESC restores original position
            try {
                dao.insert(q)
            } catch (_: Exception) {
                dao.insert(q.copy(id = 0))
            }
        }
    }

    fun clearUndo() { lastDeleted = null }
}
