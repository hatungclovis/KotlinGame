package com.hatungclovis.kotlingame.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat

/**
 * Haptic feedback utility functions
 * Provides different vibration patterns for various game events
 */
object HapticUtils {
    
    enum class HapticType {
        LIGHT,      // Key press
        MEDIUM,     // Word validation
        HEAVY,      // Game win/lose
        SUCCESS,    // Correct word
        WARNING,    // Partial match
        ERROR       // Invalid word
    }
    
    /**
     * Light haptic feedback (for key presses)
     */
    fun light(context: Context) {
        triggerHaptic(context, HapticType.LIGHT)
    }
    
    /**
     * Impact haptic feedback (for actions like submit)
     */
    fun impact(context: Context) {
        triggerHaptic(context, HapticType.MEDIUM)
    }
    
    /**
     * Success haptic feedback (for wins)
     */
    fun success(context: Context) {
        triggerHaptic(context, HapticType.SUCCESS)
    }
    
    /**
     * Error haptic feedback (for invalid inputs)
     */
    fun error(context: Context) {
        triggerHaptic(context, HapticType.ERROR)
    }
    
    /**
     * Trigger haptic feedback based on the type
     */
    fun triggerHaptic(context: Context, type: HapticType) {
        val vibrator = getVibrator(context)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (type) {
                HapticType.LIGHT -> VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.MEDIUM -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.HEAVY -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                HapticType.SUCCESS -> VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 50, 50, 50, 100),
                    intArrayOf(0, 255, 0, 255, 0, 255),
                    -1
                )
                HapticType.WARNING -> VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 100, 50),
                    intArrayOf(0, 128, 0, 128),
                    -1
                )
                HapticType.ERROR -> VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 50, 100, 50, 100),
                    intArrayOf(0, 255, 0, 255, 0, 255),
                    -1
                )
            }
            vibrator.vibrate(effect)
        } else {
            // Fallback for older Android versions
            @Suppress("DEPRECATION")
            val duration = when (type) {
                HapticType.LIGHT -> 25L
                HapticType.MEDIUM -> 50L  
                HapticType.HEAVY -> 100L
                HapticType.SUCCESS, HapticType.WARNING, HapticType.ERROR -> 150L
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }
    
    /**
     * Get vibrator instance based on Android version
     */
    private fun getVibrator(context: Context): Vibrator {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Check if device has vibrator capability
     */
    fun hasVibrator(context: Context): Boolean {
        return getVibrator(context).hasVibrator()
    }
}

/**
 * Extension functions for easier haptic feedback triggering
 */
fun Context.triggerLightHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.LIGHT)
fun Context.triggerMediumHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.MEDIUM)
fun Context.triggerHeavyHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.HEAVY)
fun Context.triggerSuccessHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.SUCCESS)
fun Context.triggerWarningHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.WARNING)
fun Context.triggerErrorHaptic() = HapticUtils.triggerHaptic(this, HapticUtils.HapticType.ERROR)
