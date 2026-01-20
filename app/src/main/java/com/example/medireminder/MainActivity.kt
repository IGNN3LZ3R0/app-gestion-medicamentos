package com.example.medireminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.medireminder.alarm.MedicineScheduler
import com.example.medireminder.data.repository.MedicineRepository
import com.example.medireminder.ui.MedicineScreen
import com.example.medireminder.ui.MedicineViewModel
import com.example.medireminder.ui.MedicineViewModelFactory
import com.example.medireminder.data.local.AppDatabase
import com.example.medireminder.data.local.MedicineEntity
import com.example.medireminder.ui.theme.ThemesTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solicitar permiso de notificaciones
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val database = AppDatabase.getInstance(applicationContext)
        val repository = MedicineRepository(database.medicineDao())
        val viewModelFactory = MedicineViewModelFactory(repository)

        setContent {
            ThemesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: MedicineViewModel = viewModel(factory = viewModelFactory)

                    MedicineScreen(
                        viewModel = viewModel,
                        onMedicinaGuardada = { medicina ->
                            if (medicina.activo) {
                                MedicineScheduler.programarRecordatorio(this, medicina)
                            }
                        },
                        onMedicinaEliminada = { medicineId ->
                            MedicineScheduler.cancelarRecordatorio(this, medicineId)
                        },
                        onEstadoCambiado = { medicina, activo ->
                            if (activo) {
                                MedicineScheduler.programarRecordatorio(this, medicina)
                            } else {
                                MedicineScheduler.cancelarRecordatorio(this, medicina.id)
                            }
                        }
                    )
                }
            }
        }

        // Reprogramar alarmas al iniciar la app
        CoroutineScope(Dispatchers.IO).launch {
            val medicinas = repository.todasLasMedicinas
            medicinas.collect { lista ->
                MedicineScheduler.reprogramarTodas(this@MainActivity, lista)
            }
        }
    }
}