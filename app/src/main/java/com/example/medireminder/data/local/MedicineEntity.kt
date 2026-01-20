package com.example.medireminder.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicinas")
data class MedicineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Nombre del medicamento
    val nombre: String,
    
    // Dosis (ej: "500mg", "2 pastillas")
    val dosis: String,
    
    // Frecuencia (ej: "Cada 8 horas", "Diario")
    val frecuencia: String,
    
    // Hora de la primera toma (formato 24h, ej: 8 para las 8:00 AM)
    val hora: Int,
    
    // Minuto de la primera toma
    val minuto: Int,
    
    // Notas adicionales (opcional)
    val notas: String = "",
    
    // Si el recordatorio está activo
    val activo: Boolean = true,
    
    // Fecha de creación
    val fechaCreacion: Long = System.currentTimeMillis(),
    
    // Días de la semana (1=Lunes, 7=Domingo). Lista separada por comas: "1,2,3,4,5"
    val diasSemana: String = "1,2,3,4,5,6,7" // Por defecto todos los días
)