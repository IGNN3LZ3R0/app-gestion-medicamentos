package com.example.expesnsetrarcker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.example.expesnsetrarcker.MainActivity
import com.example.expesnsetrarcker.R

/**
 * BroadcastReceiver mejorado con sonido y vibración garantizados.
 * Funciona incluso con la app completamente cerrada.
 *
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Primero mostramos la notificación
        mostrarNotificacion(context)

        // Luego activamos vibración manualmente
        activarVibracion(context)

        // Finalmente reprogramamos para el día siguiente
        val hora = ReminderPreferences.obtenerHora(context)
        val minuto = ReminderPreferences.obtenerMinuto(context)
        ReminderScheduler.programarRecordatorio(context, hora, minuto)
    }

    private fun activarVibracion(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator

                vibrator?.let {
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 500) // Patrón más largo
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    it.vibrate(effect)
                }
            } else {
                // Android 11 y anteriores
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                        val effect = VibrationEffect.createWaveform(pattern, -1)
                        it.vibrate(effect)
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mostrarNotificacion(context: Context) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal con sonido y vibración mejorados
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Gastos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios diarios para registrar tus gastos"

                // Configurar vibración
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)

                // Configurar sonido
                setSound(soundUri, audioAttributes)

                // Configurar luces (opcional pero ayuda)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE

                // Asegurar que se muestre en la pantalla de bloqueo
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(canal)
        }

        // Intent para abrir la app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Obtener URI de sonido
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Notificación con todas las configuraciones
        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💰 ¿Registraste tus gastos?")
            .setContentText("No olvides anotar lo que gastaste hoy")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es importante mantener un registro de tus gastos diarios. ¡Toca aquí para registrar!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

            // Configurar sonido
            .setSound(soundUri)

            // Configurar vibración
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))

            // Configurar defaults (importante para garantizar sonido y vibración)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

            // Mostrar en pantalla de bloqueo
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

            // Añadir un action button (opcional)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Registrar ahora",
                pendingIntent
            )

            .build()

        notificationManager.notify(NOTIFICATION_ID, notificacion)
    }

    companion object {
        const val CHANNEL_ID = "expense_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}
