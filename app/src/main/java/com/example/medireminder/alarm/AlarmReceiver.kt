package com.example.medireminder.alarm

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
import com.example.medireminder.MainActivity
import com.example.medireminder.R
import com.example.medireminder.data.local.AppDatabase
import com.example.medireminder.data.repository.MedicineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val medicineId = intent.getIntExtra("MEDICINE_ID", -1)
        val medicineName = intent.getStringExtra("MEDICINE_NAME") ?: "Medicamento"
        val medicineDosis = intent.getStringExtra("MEDICINE_DOSIS") ?: ""

        // Mostrar notificación
        mostrarNotificacion(context, medicineId, medicineName, medicineDosis)

        // Activar vibración
        activarVibracion(context)

        // Reprogramar para el día siguiente
        CoroutineScope(Dispatchers.IO).launch {
            val database = AppDatabase.getInstance(context)
            val repository = MedicineRepository(database.medicineDao())
            val medicina = repository.obtenerPorId(medicineId)
            
            medicina?.let {
                if (it.activo) {
                    MedicineScheduler.programarRecordatorio(context, it)
                }
            }
        }
    }

    private fun activarVibracion(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator

                vibrator?.let {
                    val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                    val effect = VibrationEffect.createWaveform(pattern, -1)
                    it.vibrate(effect)
                }
            } else {
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

    private fun mostrarNotificacion(
        context: Context,
        medicineId: Int,
        medicineName: String,
        medicineDosis: String
    ) {
        val notificationManager = context
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val canal = NotificationChannel(
                CHANNEL_ID,
                "Recordatorios de Medicinas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Recordatorios para tomar tus medicinas"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 500)
                setSound(soundUri, audioAttributes)
                enableLights(true)
                lightColor = android.graphics.Color.BLUE
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
            medicineId,
            openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Construir notificación
        val dosisText = if (medicineDosis.isNotEmpty()) " - $medicineDosis" else ""
        
        val notificacion = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("💊 Hora de tomar tu medicina")
            .setContentText("$medicineName$dosisText")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Es hora de tomar: $medicineName$dosisText\n\nToca para abrir la aplicación")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Abrir app",
                pendingIntent
            )
            .build()

        notificationManager.notify(medicineId, notificacion)
    }

    companion object {
        const val CHANNEL_ID = "medicine_reminder_channel"
    }
}