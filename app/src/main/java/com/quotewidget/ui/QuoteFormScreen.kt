package com.quotewidget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotewidget.data.Quote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteFormScreen(
    editing: Quote?,
    onSave: (text: String, author: String) -> String?,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    var text by remember { mutableStateOf(editing?.text ?: "") }
    var author by remember { mutableStateOf(editing?.author ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing == null) "Tambah Quote" else "Edit Quote") },
                navigationIcon = { TextButton(onClick = onBack) { Text("Batal") } }
            )
        }
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp)) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(200) },
                label = { Text("Quote (maks 200)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Text("${text.length}/200", style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Penulis (opsional)") },
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val msg = onSave(text, author)
                    if (msg == null) onBack() else error = msg
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }
        }
    }
}
