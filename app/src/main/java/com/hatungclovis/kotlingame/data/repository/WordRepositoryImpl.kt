package com.hatungclovis.kotlingame.data.repository

import com.hatungclovis.kotlingame.data.source.WordDataSource
import com.hatungclovis.kotlingame.domain.models.WordAnalysis
import com.hatungclovis.kotlingame.domain.models.WordStatistics
import com.hatungclovis.kotlingame.domain.repository.WordRepository
import com.hatungclovis.kotlingame.utils.GameUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of WordRepository using WordDataSource
 */
@Singleton
class WordRepositoryImpl @Inject constructor(
    private val wordDataSource: WordDataSource
) : WordRepository {
    
    // Cache for loaded words to avoid repeated file I/O
    private var cachedCommonWords: List<String>? = null
    private var cachedAllWords: Set<String>? = null // Use Set for faster lookups
    
    override suspend fun getCommonWords(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (cachedCommonWords == null) {
                val result = wordDataSource.loadCommonWords()
                if (result.isSuccess) {
                    cachedCommonWords = result.getOrNull()
                }
                result
            } else {
                Result.success(cachedCommonWords!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAllWords(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            if (cachedAllWords == null) {
                val result = wordDataSource.loadAllWords()
                if (result.isSuccess) {
                    cachedAllWords = result.getOrNull()?.toSet()
                    Result.success(cachedAllWords!!.toList())
                } else {
                    result
                }
            } else {
                Result.success(cachedAllWords!!.toList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRandomWord(length: Int): Result<String> = withContext(Dispatchers.IO) {
        try {
            getCommonWords().mapCatching { words ->
                GameUtils.getRandomWord(words, length)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun isValidWord(word: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            // Ensure all words are loaded
            if (cachedAllWords == null) {
                val result = getAllWords()
                if (result.isFailure) {
                    return@withContext result.map { false }
                }
            }
            
            // Check if word exists in the all-words dictionary (case-insensitive)
            val normalizedWord = word.lowercase().trim()
            Result.success(cachedAllWords?.contains(normalizedWord) == true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWordsOfLength(length: Int): Result<List<String>> = withContext(Dispatchers.IO) {
        getCommonWords().mapCatching { words ->
            words.filter { it.length == length }
        }
    }
    
    override suspend fun getAvailableWordLengths(): Result<List<Int>> = withContext(Dispatchers.IO) {
        getCommonWords().mapCatching { words ->
            words.map { it.length }.distinct().sorted()
        }
    }
    
    override suspend fun analyzeWordFrequency(): Result<WordAnalysis> = withContext(Dispatchers.IO) {
        getCommonWords().mapCatching { words ->
            GameUtils.analyzeWordFrequency(words)
        }
    }
    
    override suspend fun getWordStatistics(): Result<WordStatistics> = withContext(Dispatchers.IO) {
        wordDataSource.getWordStatistics()
    }
    
    override suspend fun validateWordFiles(): Result<Boolean> = withContext(Dispatchers.IO) {
        wordDataSource.validateWordFiles()
    }
    
    /**
     * Clear cached words (useful for testing or memory management)
     */
    fun clearCache() {
        cachedCommonWords = null
        cachedAllWords = null
    }
    
    /**
     * Pre-load words into cache for better performance
     */
    suspend fun preloadWords(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val commonWordsResult = getCommonWords()
            val allWordsResult = getAllWords()
            
            if (commonWordsResult.isSuccess && allWordsResult.isSuccess) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to preload words"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
