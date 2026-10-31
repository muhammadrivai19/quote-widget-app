package com.quotewidget.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val author: String?,
    val createdAt: Long = System.currentTimeMillis()
)
