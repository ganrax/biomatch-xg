package com.example.data.db

import kotlinx.coroutines.flow.Flow

class AnalysisRepository(private val dao: SavedAnalysisDao) {
    val allSaved: Flow<List<SavedAnalysisEntity>> = dao.getAllSaved()

    fun getSavedByType(type: String): Flow<List<SavedAnalysisEntity>> = dao.getSavedByType(type)

    suspend fun saveAnalysis(entity: SavedAnalysisEntity): Long = dao.insertAnalysis(entity)

    suspend fun updateAnalysis(entity: SavedAnalysisEntity) = dao.updateAnalysis(entity)

    suspend fun getAnalysisById(id: Long): SavedAnalysisEntity? = dao.getAnalysisById(id)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun clearAll() = dao.clearAll()
}
