package com.quotewidget.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Quote>>

    @Query("SELECT * FROM quotes ORDER BY createdAt DESC")
    suspend fun getAll(): List<Quote>

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getById(id: Long): Quote?

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun count(): Int

    @Insert
    suspend fun insert(quote: Quote): Long

    @Insert
    suspend fun insertAll(quotes: List<Quote>)

    @Update
    suspend fun update(quote: Quote)

    @Delete
    suspend fun delete(quote: Quote)

    @Query("DELETE FROM quotes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
