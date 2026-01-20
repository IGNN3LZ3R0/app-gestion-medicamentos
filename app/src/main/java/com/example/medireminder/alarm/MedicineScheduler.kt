package com.example.medireminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medireminder.data.local.MedicineEntity
import java.util.Calendar

object MedicineScheduler {

    /**
     * Programa un recordatorio para una medicina específica
     */
    fun programarRecordatorio(context: Context, medicina: MedicineEntity) {
        if (!medicina.activo) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("MEDICINE_ID", medicina.id)
            putExtra("MEDICINE_NAME", medicina.nombre)
            putExtra("MEDICINE_DOSIS", medicina.dosis)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicina.id, // Usamos el ID como request code único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular el tiempo para la próxima alarma
        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, medicina.hora)
            set(Calendar.MINUTE, medicina.minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si ya pasó la hora hoy, programar para mañana
        if (calendario.timeInMillis <= System.currentTimeMillis()) {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Programar alarma diaria exacta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendario.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                calendario.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Cancela el recordatorio de una medicina específica
     */
    fun cancelarRecordatorio(context: Context, medicineId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            medicineId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * Reprograma todas las medicinas activas (útil después de reiniciar el dispositivo)
     */
    suspend fun reprogramarTodas(context: Context, medicinas: List<MedicineEntity>) {
        medicinas.filter { it.activo }.forEach { medicina ->
            programarRecordatorio(context, medicina)
        }
    }
}