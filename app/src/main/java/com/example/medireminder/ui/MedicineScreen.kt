
package com.example.medireminder.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medireminder.data.local.MedicineEntity



// Pantalla principal de medicinas
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    viewModel: MedicineViewModel,
    onMedicinaGuardada: (MedicineEntity) -> Unit,
    onMedicinaEliminada: (Int) -> Unit,
    onEstadoCambiado: (MedicineEntity, Boolean) -> Unit
) {
    val nombre by viewModel.nombre.collectAsState()
    val dosis by viewModel.dosis.collectAsState()
    val frecuenciaSeleccionada by viewModel.frecuenciaSeleccionada.collectAsState()
    val hora by viewModel.hora.collectAsState()
    val minuto by viewModel.minuto.collectAsState()
    val notas by viewModel.notas.collectAsState()
    val medicinas by viewModel.medicinas.collectAsState(initial = emptyList())
    val medicinaEnEdicion by viewModel.medicinaEnEdicion.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💊 Mis Medicinas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            FormularioMedicina(
                nombre = nombre,
                dosis = dosis,
                frecuenciaSeleccionada = frecuenciaSeleccionada,
                frecuencias = viewModel.frecuencias,
                hora = hora,
                minuto = minuto,
                notas = notas,
                esEdicion = medicinaEnEdicion != null,
                onNombreChange = { viewModel.actualizarNombre(it) },
                onDosisChange = { viewModel.actualizarDosis(it) },
                onFrecuenciaChange = { viewModel.seleccionarFrecuencia(it) },
                onHoraChange = { h, m -> viewModel.actualizarHora(h, m) },
                onNotasChange = { viewModel.actualizarNotas(it) },
                onGuardar = {
                    val id = viewModel.guardarMedicina()
                },
                onCancelarEdicion = { viewModel.cancelarEdicion() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Text(
                    text = "Total medicinas: ${medicinas.size}",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Historial",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (medicinas.isEmpty()) {
                Text(
                    text = "No hay medicinas registradas",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                medicinas.forEach { medicina ->
                    MedicinaItem(
                        medicina = medicina,
                        onEditar = { viewModel.iniciarEdicion(medicina) },
                        onEliminar = {
                            viewModel.eliminarMedicina(medicina)
                            onMedicinaEliminada(medicina.id)
                        },
                        onEstadoCambiado = { activo ->
                            viewModel.cambiarEstado(medicina, activo)
                            onEstadoCambiado(medicina, activo)
                        }
                    )
                }
            }
        }
    }
}

onHoraChange(nuevaHora, nuevoMinuto)
mostrarTimePicker = false
},
onDismiss = { mostrarTimePicker = false }
)
}

// Formulario para agregar/editar medicina
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioMedicina(
    nombre: String,
    dosis: String,
    frecuenciaSeleccionada: String,
    frecuencias: List<String>,
    hora: Int,
    minuto: Int,
    notas: String,
    esEdicion: Boolean,
    onNombreChange: (String) -> Unit,
    onDosisChange: (String) -> Unit,
    onFrecuenciaChange: (String) -> Unit,
    onHoraChange: (Int, Int) -> Unit,
    onNotasChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onCancelarEdicion: () -> Unit
) {
    var expandedFrecuencia by remember { mutableStateOf(false) }
    var mostrarTimePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (esEdicion) "Editar Medicina" else "Nueva Medicina",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del medicamento") },
                leadingIcon = { Text("💊") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = dosis,
                onValueChange = onDosisChange,
                label = { Text("Dosis (ej: 500mg, 2 pastillas)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expandedFrecuencia,
                onExpandedChange = { expandedFrecuencia = it }
            ) {
                OutlinedTextField(
                    value = frecuenciaSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Frecuencia") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrecuencia)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expandedFrecuencia,
                    onDismissRequest = { expandedFrecuencia = false }
                ) {
                    frecuencias.forEach { frecuencia ->
                        DropdownMenuItem(
                            text = { Text(frecuencia) },
                            onClick = {
                                onFrecuenciaChange(frecuencia)
                                expandedFrecuencia = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hora del recordatorio",
                    style = MaterialTheme.typography.bodyMedium
                )

                TextButton(onClick = { mostrarTimePicker = true }) {
                    Text(
                        text = String.format("%02d:%02d", hora, minuto),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            OutlinedTextField(
                value = notas,
                onValueChange = onNotasChange,
                label = { Text("Notas (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (esEdicion) {
                    Button(
                        onClick = onCancelarEdicion,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                }

                Button(
                    onClick = onGuardar,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (esEdicion) "Actualizar" else "Guardar")
                }
            }
        }
    }

    if (mostrarTimePicker) {
        TimePickerDialog(
            horaInicial = hora,
            minutoInicial = minuto,
            onConfirm = { nuevaHora, nuevoMinuto ->
                onHoraChange(nuevaHora, nuevoMinuto)
                mostrarTimePicker = false
            },
            onDismiss = { mostrarTimePicker = false }
        )
    }
}
horaInicial: Int,
minutoInicial: Int,
onConfirm: (Int, Int) -> Unit,
onDismiss: () -> Unit
is24Hour = true

// Diálogo para seleccionar hora
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    horaInicial: Int,
    minutoInicial: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = horaInicial,
        initialMinute = minutoInicial,
        is24Hour = true
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar hora") },
        text = {
            TimePicker(state = timePickerState)
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(timePickerState.hour, timePickerState.minute) }
            ) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
horizontalArrangement = Arrangement.SpaceBetween,
verticalAlignment = Alignment.CenterVertically

// Item para mostrar cada medicina
@Composable
fun MedicinaItem(
    medicina: MedicineEntity,
    onEditar: () -> Unit,
    onEliminar: () -> Unit,
    onEstadoCambiado: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicina.nombre,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "${medicina.dosis} • ${medicina.frecuencia}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format("%02d:%02d", medicina.hora, medicina.minuto),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
                if (medicina.notas.isNotEmpty()) {
                    Text(
                        text = medicina.notas,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Switch(
                    checked = medicina.activo,
                    onCheckedChange = onEstadoCambiado
                )
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}