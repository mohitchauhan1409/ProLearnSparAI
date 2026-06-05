package com.prolearn.spar.service

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HapticsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun tapLight() {
        vibrate(VibrationEffect.createOneShot(10, 80))
    }

    fun tapMedium() {
        vibrate(VibrationEffect.createOneShot(20, 150))
    }

    fun tapStrong() {
        vibrate(VibrationEffect.createOneShot(40, 255))
    }

    fun hintSpend() {
        vibrate(VibrationEffect.createWaveform(longArrayOf(0, 20, 60, 20), -1))
    }

    fun sessionStart() {
        vibrate(VibrationEffect.createOneShot(60, 200))
    }

    fun sessionComplete() {
        vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30, 50, 80), -1))
    }

    fun error() {
        vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1))
    }

    private fun vibrate(effect: VibrationEffect) {
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(effect)
        }
    }
}
