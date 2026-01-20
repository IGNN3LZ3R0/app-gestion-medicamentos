package com.example.medireminder.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.medireminder.alarm.AlarmReceiver
import com.example.medireminder.data.local.MedicineEntity
import java.util.Calendar

/**
 * Clase utilitaria para programar el recordatorio diario usando AlarmManager.
 * Usa alarmas exactas para garantizar que las notificaciones lleguen a la hora configurada,
 * incluso con optimización de batería activa (Doze mode).
 */
object MedicineScheduler {

    private const val ALARM_REQUEST_CODE = 1001

    /**
     * Programa un recordatorio diario a la hora indicada.
     */
    fun programarRecordatorio(context: Context, hora: Int, minuto: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Calcular el tiempo para la próxima alarma
        val calendario = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Si ya pasó la hora, programar para mañana
        if (calendario.timeInMillis <= System.currentTimeMillis()) {
            calendario.add(Calendar.DAY_OF_MONTH, 1)
        }

        // Usar setExactAndAllowWhileIdle para que funcione en Doze mode
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
     * Programa un recordatorio basado en la entidad de medicina.
     */
    fun programarRecordatorio(context: Context, medicina: MedicineEntity) {
        programarRecordatorio(context, medicina.hora, medicina.minuto)
        // Aquí puedes agregar lógica adicional si necesitas usar más campos de medicina
    }


    /**
     * Cancela el recordatorio usando el ID de la medicina.
     */
    fun cancelarRecordatorio(context: Context, medicineId: Int) {
        // Usa medicineId como requestCode para cancelar el PendingIntent específico
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
     * Reprograma todos los recordatorios activos.
     */
    fun reprogramarTodas(context: Context, medicinas: List<MedicineEntity>) {
        medicinas.filter { it.activo }.forEach { medicina ->
            programarRecordatorio(context, medicina)
        }
    }
}
