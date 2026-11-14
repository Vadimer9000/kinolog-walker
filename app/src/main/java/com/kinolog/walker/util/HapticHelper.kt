package com.kinolog.walker.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Taptic-style Haptic Feedback utility.
 * Uses system micro-pulses (HapticFeedbackConstants and VibrationEffect predefined effects)
 * without harsh fixed-duration vibrations.
 */
object HapticHelper {

    fun isHapticEnabled(context: Context): Boolean {
        return PreferencesManager(context).isHapticFeedbackEnabled
    }

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Самый слабый и короткий микро-щелчок для прокрутки барабана часов/минут и переключения чипов.
     */
    fun tick(view: View?, context: Context) {
        if (!isHapticEnabled(context)) return

        val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            HapticFeedbackConstants.SEGMENT_FREQUENT_TICK
        } else {
            HapticFeedbackConstants.CLOCK_TICK
        }

        var performed = false
        if (view != null) {
            try {
                performed = view.performHapticFeedback(
                    feedbackConstant,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Exception) {}
        }

        if (!performed) {
            try {
                val vibrator = getVibrator(context)
                if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Мягкий клик для обычных кнопок, выбора адреса из выпадающего списка,
     * переключения вкладок "План"/"Клиенты" и режима "Пешком"/"Транспорт".
     */
    fun lightClick(view: View?, context: Context) {
        if (!isHapticEnabled(context)) return

        var performed = false
        if (view != null) {
            try {
                performed = view.performHapticFeedback(
                    HapticFeedbackConstants.KEYBOARD_TAP,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Exception) {}
        }

        if (!performed) {
            try {
                val vibrator = getVibrator(context)
                if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Приятный двойной/подтверждающий отклик при отметке выгула как "Выполнен",
     * сохранении карточки клиента или запланированного выгула.
     */
    fun success(view: View?, context: Context) {
        if (!isHapticEnabled(context)) return

        var performed = false
        if (view != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                performed = view.performHapticFeedback(
                    HapticFeedbackConstants.CONFIRM,
                    HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING
                )
            } catch (_: Exception) {}
        }

        if (!performed) {
            try {
                val vibrator = getVibrator(context)
                if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                }
            } catch (_: Exception) {}
        }
    }

    /**
     * Тактильный отклик при ошибке валидации формы или предупреждении.
     */
    fun warning(view: View?, context: Context) {
        lightClick(view, context)
    }
}
