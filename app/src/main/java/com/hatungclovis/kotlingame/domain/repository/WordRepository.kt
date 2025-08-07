package com.hatungclovis.kotlingame.domain.repository

import com.hatungclovis.kotlingame.domain.models.WordAnalysis
import com.hatungclovis.kotlingame.domain.models.WordStatistics

/**
 * Repository interface for word-related operations
 */
interface WordRepository {
    
    /**
     * Load common words (used for game word selection)
     */
    suspend fun getCommonWords(): Result<List<String>>
    
    /**
     * Load all words (used for word validation)
     */
    suspend fun getAllWords(): Result<List<String>>
    
    /**
     * Get random word of specified length from common words
     */
    suspend fun getRandomWord(length: Int): Result<String>
    
    /**
     * Check if a word exists in the all-words dictionary
     */
    suspend fun isValidWord(word: String): Result<Boolean>
    
    /**
     * Get words of specific length from common words
     */
    suspend fun getWordsOfLength(length: Int): Result<List<String>>
    
    /**
     * Get available word lengths
     */
    suspend fun getAvailableWordLengths(): Result<List<Int>>
    
    /**
     * Analyze word frequency by length and first letter
     */
    suspend fun analyzeWordFrequency(): Result<WordAnalysis>
    
    /**
     * Get word statistics
     */
    suspend fun getWordStatistics(): Result<WordStatistics>
    
    /**
     * Validate that word files are properly loaded
     */
    suspend fun validateWordFiles(): Result<Boolean>
}
