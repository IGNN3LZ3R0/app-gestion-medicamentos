package com.example.medireminder.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medireminder.data.local.MedicineEntity
import com.example.medireminder.data.repository.MedicineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MedicineViewModel(
    private val repository: MedicineRepository
) : ViewModel() {

    private val _nombre = MutableStateFlow("")
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    private val _dosis = MutableStateFlow("")
    val dosis: StateFlow<String> = _dosis.asStateFlow()

    private val _frecuenciaSeleccionada = MutableStateFlow("Diario")
    val frecuenciaSeleccionada: StateFlow<String> = _frecuenciaSeleccionada.asStateFlow()

    private val _hora = MutableStateFlow(8)
    val hora: StateFlow<Int> = _hora.asStateFlow()

    private val _minuto = MutableStateFlow(0)
    val minuto: StateFlow<Int> = _minuto.asStateFlow()

    private val _notas = MutableStateFlow("")
    val notas: StateFlow<String> = _notas.asStateFlow()

    private val _medicinaEnEdicion = MutableStateFlow<MedicineEntity?>(null)
    val medicinaEnEdicion: StateFlow<MedicineEntity?> = _medicinaEnEdicion.asStateFlow()

    val medicinas = repository.todasLasMedicinas
    val frecuencias = listOf(
        "Diario",
        "Cada 8 horas",
        "Cada 12 horas",
        "Lunes a Viernes",
        "Fines de semana",
        "Personalizado"
    )

    fun actualizarNombre(valor: String) {
        _nombre.value = valor
    }

    fun actualizarDosis(valor: String) {
        _dosis.value = valor
    }

    fun seleccionarFrecuencia(frecuencia: String) {
        _frecuenciaSeleccionada.value = frecuencia
    }

    fun actualizarHora(hora: Int, minuto: Int) {
        _hora.value = hora
        _minuto.value = minuto
    }

    fun actualizarNotas(valor: String) {
        _notas.value = valor
    }

    fun iniciarEdicion(medicina: MedicineEntity) {
        _medicinaEnEdicion.value = medicina
        _nombre.value = medicina.nombre
        _dosis.value = medicina.dosis
        _frecuenciaSeleccionada.value = medicina.frecuencia
        _hora.value = medicina.hora
        _minuto.value = medicina.minuto
        _notas.value = medicina.notas
    }

    fun cancelarEdicion() {
        _medicinaEnEdicion.value = null
        limpiarFormulario()
    }

    fun guardarMedicina(): Long? {
        if (_nombre.value.isBlank()) return null

        var medicineId: Long? = null

        viewModelScope.launch {
            val medicinaActual = _medicinaEnEdicion.value

            if (medicinaActual != null) {
                val medicinaActualizada = medicinaActual.copy(
                    nombre = _nombre.value.trim(),
                    dosis = _dosis.value.trim(),
                    frecuencia = _frecuenciaSeleccionada.value,
                    hora = _hora.value,
                    minuto = _minuto.value,
                    notas = _notas.value.trim()
                )
                repository.actualizar(medicinaActualizada)
                medicineId = medicinaActual.id.toLong()
                _medicinaEnEdicion.value = null
            } else {
                val nuevaMedicina = MedicineEntity(
                    nombre = _nombre.value.trim(),
                    dosis = _dosis.value.trim(),
                    frecuencia = _frecuenciaSeleccionada.value,
                    hora = _hora.value,
                    minuto = _minuto.value,
                    notas = _notas.value.trim()
                )
                medicineId = repository.agregar(nuevaMedicina)
            }

            limpiarFormulario()
        }

        return medicineId
    }

    fun eliminarMedicina(medicina: MedicineEntity) {
        viewModelScope.launch {
            repository.eliminar(medicina)
        }
    }

    fun cambiarEstado(medicina: MedicineEntity, activo: Boolean) {
        viewModelScope.launch {
            repository.cambiarEstado(medicina.id, activo)
        }
    }

    private fun limpiarFormulario() {
        _nombre.value = ""
        _dosis.value = ""
        _frecuenciaSeleccionada.value = "Diario"
        _hora.value = 8
        _minuto.value = 0
        _notas.value = ""
    }
}

class MedicineViewModelFactory(
    private val repository: MedicineRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MedicineViewModel::class.java)) {
            return MedicineViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido")
    }
}