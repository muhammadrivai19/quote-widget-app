package com.quotewidget.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        Text("1. Tulis quotes", style = MaterialTheme.typography.headlineSmall)
        Text("Tambah quotes favoritmu, maks 200 karakter per quote.")
        Spacer(Modifier.height(16.dp))
        Text("2. Pasang widget", style = MaterialTheme.typography.headlineSmall)
        Text("Tekan lama home screen → Widgets → QuoteWidget → pasang.")
        Spacer(Modifier.height(32.dp))
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
            Text("Mulai")
        }
    }
}
