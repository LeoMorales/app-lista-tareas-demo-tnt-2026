package com.example.demolistatareas.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.demolistatareas.presentation.state.TareasUiState
import com.example.demolistatareas.presentation.ui.MapaGlobalScreen
import com.example.demolistatareas.presentation.ui.MapaScreen
import com.example.demolistatareas.presentation.ui.TareasLaboralesScreen
import com.example.demolistatareas.presentation.ui.TareasScreen
import com.example.demolistatareas.presentation.viewmodel.TareasLaboralesViewModel
import com.example.demolistatareas.presentation.viewmodel.TareasViewModel

/**
* Orquestador de la navegación de la aplicación (Router).
* Define las rutas existentes y qué Composable renderizar en cada una.
* Aislar esta responsabilidad facilita la lectura de la Activity principal.
*/
@Composable
fun AppNavigation(
    viewModelFactory: ViewModelProvider.Factory
) {
    // NavController como motor que gestiona la pila de pantallas (BackStack)
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "tareas_locales") {

        composable("tareas_locales") { backStackEntry ->
            // El ViewModel se instancia a nivel de la ruta
            val viewModel: TareasViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsState()

            // Mapas #1: el SavedStateHandle funciona como un "buzón" para la comunicación:
            // Observar el SavedStateHandle buscando coordenadas que provengan del mapa
            val savedStateHandle = backStackEntry.savedStateHandle
            // Se utiliza getStateFlow especificando la clave y un valor inicial nulo.
            val latitudRetorno by savedStateHandle
                .getStateFlow<Double?>(
                    "lat",
                    null
                ).collectAsState()
            val longitudRetorno by savedStateHandle
                .getStateFlow<Double?>("lon", null)
                .collectAsState()

            TareasScreen(
                uiState = uiState,
                latitudSeleccionada = latitudRetorno,
                longitudSeleccionada = longitudRetorno,
                // Se simula la captura de coordenadas al crear la tarea (si existen)
                onAgregarTarea = { titulo ->
                    viewModel.onAgregarTarea(
                        titulo = titulo,
                        lat = latitudRetorno,
                        lon = longitudRetorno
                    )
                    // Se limpia el estado guardado despues de consumirlo para evitar guardados duplicados
                    savedStateHandle.remove<Double>("lat")
                    savedStateHandle.remove<Double>("lon")
                },
                onAlternarEstado = viewModel::onAlternarEstado,
                // Se ejecuta la acción de navegación instruyendo al NavController
                onNavegarALaborales = { navController.navigate("tareas_laborales") },
                // Evento para abrir el mapa
                onAbrirMapa = { navController.navigate("seleccion_mapa") },
                onVerMapaGlobal = { navController.navigate("mapa_global") }
            )
        }

        composable("tareas_laborales") {
            // Se requiere inyectar el ViewModel correspondiente a esta ruta
            val viewModel: TareasLaboralesViewModel = viewModel(
                factory = viewModelFactory // Se utiliza el factory unificado provisto
            )
            val uiState by viewModel.uiState.collectAsState()

            TareasLaboralesScreen(
                uiState = uiState,
                onVolver = { navController.popBackStack() }
            )
        }

        // Mapas #2. RUta para la pantalla del Mapa
        composable("seleccion_mapa") {
            MapaScreen(
                onUbicacionConfirmada = { lat, lon ->
                    // Mapas #3. Enviar datos al buzón:
                    // Antes de volver atrás, se inyectan los datos en el destino anterior
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("lat", lat)
                        set("lon", lon)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("mapa_global") {
            // Reutilizamos el ViewModel principal porque ya tiene el StateFlow con todas las tareas
            val viewModel: TareasViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsState()

            // Solo mostramos el mapa si el estado es Exito (es decir, si hay tareas cargadas)
            if (uiState is TareasUiState.Exito) {
                MapaGlobalScreen(
                    tareas = (uiState as TareasUiState.Exito).tareas,
                    onVolver = { navController.popBackStack() }
                )
            }
        }
    }
}