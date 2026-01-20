package com.example.medireminder.data.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.medireminder.data.local.MedicineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    
    @Query("SELECT * FROM medicinas ORDER BY hora ASC, minuto ASC")
    fun obtenerTodas(): Flow<List<MedicineEntity>>
    
    @Query("SELECT * FROM medicinas WHERE activo = 1 ORDER BY hora ASC, minuto ASC")
    fun obtenerActivas(): Flow<List<MedicineEntity>>
    
    @Query("SELECT * FROM medicinas WHERE id = :id")
    suspend fun obtenerPorId(id: Int): MedicineEntity?
    
    @Insert
    suspend fun insertar(medicina: MedicineEntity): Long
    
    @Update
    suspend fun actualizar(medicina: MedicineEntity)
    
    @Delete
    suspend fun eliminar(medicina: MedicineEntity)
    
    @Query("UPDATE medicinas SET activo = :activo WHERE id = :id")
    suspend fun cambiarEstado(id: Int, activo: Boolean)
}