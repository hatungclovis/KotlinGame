package com.hatungclovis.kotlingame.data.source

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hatungclovis.kotlingame.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream

/**
 * Data source for loading words from JSON assets
 */
class WordDataSource(private val context: Context) {
    
    private val gson = Gson()
    
    /**
     * Load common words from assets
     */
    suspend fun loadCommonWords(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.assets.open("words/${GameConstants.COMMON_WORDS_FILE}")
            val json = inputStream.bufferedReader().use { it.readText() }
            val wordList: List<String> = gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
            Result.success(wordList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load all words from all_words directory
     */
    suspend fun loadAllWords(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val allWords = mutableListOf<String>()
            
            // Load all word files from all_words directory
            for (i in 1..GameConstants.ALL_WORDS_FILE_COUNT) {
                val fileName = "${GameConstants.ALL_WORDS_FILE_PREFIX}${String.format("%03d", i)}.json"
                val filePath = "words/${GameConstants.ALL_WORDS_FOLDER}/$fileName"
                
                try {
                    val inputStream = context.assets.open(filePath)
                    val json = inputStream.bufferedReader().use { it.readText() }
                    val wordList: List<String> = gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
                    allWords.addAll(wordList)
                } catch (e: IOException) {
                    // Log error but continue with other files
                    android.util.Log.w("WordDataSource", "Failed to load $filePath: ${e.message}")
                }
            }
            
            if (allWords.isEmpty()) {
                Result.failure(Exception("No word files were loaded successfully"))
            } else {
                Result.success(allWords.distinct()) // Remove duplicates if any
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load words of specific length from common words
     */
    suspend fun loadWordsOfLength(length: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        loadCommonWords().mapCatching { words ->
            words.filter { it.length == length }
        }
    }
    
    /**
     * Get available word lengths from common words
     */
    suspend fun getAvailableWordLengths(): Result<List<Int>> = withContext(Dispatchers.IO) {
        loadCommonWords().mapCatching { words ->
            words.map { it.length }.distinct().sorted()
        }
    }
    
    /**
     * Get word statistics
     */
    suspend fun getWordStatistics(): Result<WordStatistics> = withContext(Dispatchers.IO) {
        try {
            val commonWordsResult = loadCommonWords()
            val allWordsResult = loadAllWords()
            
            if (commonWordsResult.isFailure || allWordsResult.isFailure) {
                return@withContext Result.failure(
                    Exception("Failed to load word statistics: ${commonWordsResult.exceptionOrNull()?.message ?: allWordsResult.exceptionOrNull()?.message}")
                )
            }
            
            val commonWords = commonWordsResult.getOrNull() ?: emptyList()
            val allWords = allWordsResult.getOrNull() ?: emptyList()
            
            val availableLengths = commonWords.map { it.length }.distinct().sorted()
            val lengthRange = if (availableLengths.isNotEmpty()) {
                availableLengths.first()..availableLengths.last()
            } else {
                3..14
            }
            
            val statistics = WordStatistics(
                totalCommonWords = commonWords.size,
                totalAllWords = allWords.size,
                lengthRange = lengthRange,
                availableLengths = availableLengths
            )
            
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if assets contain expected files
     */
    suspend fun validateWordFiles(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Check common words file
            context.assets.open("words/${GameConstants.COMMON_WORDS_FILE}").close()
            
            // Check at least some all_words files exist
            var foundFiles = 0
            for (i in 1..GameConstants.ALL_WORDS_FILE_COUNT) {
                val fileName = "${GameConstants.ALL_WORDS_FILE_PREFIX}${String.format("%03d", i)}.json"
                val filePath = "words/${GameConstants.ALL_WORDS_FOLDER}/$fileName"
                
                try {
                    context.assets.open(filePath).close()
                    foundFiles++
                } catch (e: IOException) {
                    // File doesn't exist, continue checking others
                }
            }
            
            // Require at least 70% of expected files to be present
            val requiredFiles = (GameConstants.ALL_WORDS_FILE_COUNT * 0.7).toInt()
            Result.success(foundFiles >= requiredFiles)
            
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
