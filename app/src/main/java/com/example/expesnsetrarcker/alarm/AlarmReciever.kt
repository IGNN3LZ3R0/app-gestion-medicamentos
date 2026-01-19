package com.example.expesnsetrarcker.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.expesnsetrarcker.R
import com.example.expesnsetrarcker.MainActivity

/**
 * BroadcastReceiver mejorado con sonido y vibración.
 * Funciona incluso con la app completamente cerrada.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        mostrarNotificacion(context)

        // Reprogramar para el día siguiente
        val hora = ReminderPreferences.obtenerHora(context)
        val minuto = ReminderPreferences.obtenerMinuto(context)
        ReminderScheduler.programarRecordatorio(context, hora, minuto)
    }

    private fun mostrarNotificacion(context: Context) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal con sonido y vibración
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios diarios de gastos"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500) // Patrón de vibración
                setSound(
                    RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                    null
                )
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

        // Notificación con sonido y vibración
        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("¿Registraste tus gastos?")
            .setContentText("No olvides anotar lo que gastaste hoy")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // Sonido, vibración y luces
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notificacion)
    }

    companion object {
        const val CHANNEL_ID = "expense_reminder_channel"
        const val NOTIFICATION_ID = 1001
    }
}