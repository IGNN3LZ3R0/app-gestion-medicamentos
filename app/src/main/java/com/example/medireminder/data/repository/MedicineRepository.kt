package com.example.medireminder.data.repository

import com.example.medireminder.data.local.MedicineEntity
import kotlinx.coroutines.flow.Flow

class MedicineRepository(private val dao: MedicineDao) {

    val todasLasMedicinas: Flow<List<MedicineEntity>> = dao.obtenerTodas()
    val medicinasActivas: Flow<List<MedicineEntity>> = dao.obtenerActivas()

    suspend fun agregar(medicina: MedicineEntity): Long {
        return dao.insertar(medicina)
    }

    suspend fun actualizar(medicina: MedicineEntity) {
        dao.actualizar(medicina)
    }

    suspend fun eliminar(medicina: MedicineEntity) {
        dao.eliminar(medicina)
    }

    suspend fun obtenerPorId(id: Int): MedicineEntity? {
        return dao.obtenerPorId(id)
    }

    suspend fun cambiarEstado(id: Int, activo: Boolean) {
        dao.cambiarEstado(id, activo)
    }
}