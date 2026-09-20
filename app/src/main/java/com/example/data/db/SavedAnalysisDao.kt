package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAnalysisDao {
    @Query("SELECT * FROM saved_analyses ORDER BY timestamp DESC")
    fun getAllSaved(): Flow<List<SavedAnalysisEntity>>

    @Query("SELECT * FROM saved_analyses WHERE type = :type ORDER BY timestamp DESC")
    fun getSavedByType(type: String): Flow<List<SavedAnalysisEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(entity: SavedAnalysisEntity): Long

    @androidx.room.Update
    suspend fun updateAnalysis(entity: SavedAnalysisEntity)

    @Query("SELECT * FROM saved_analyses WHERE id = :id")
    suspend fun getAnalysisById(id: Long): SavedAnalysisEntity?

    @Query("DELETE FROM saved_analyses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM saved_analyses")
    suspend fun clearAll()
}
