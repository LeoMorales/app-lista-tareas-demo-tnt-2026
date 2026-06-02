package com.example.demolistatareas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.usecase.AgregarTareaUseCase
import com.example.demolistatareas.domain.usecase.AlternarEstadoTareaUseCase
import com.example.demolistatareas.domain.usecase.ObtenerTareasUseCase
import com.example.demolistatareas.presentation.state.TareasUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Gestiona el estado de la pantalla y conecta la UI con la lógica de negocio (Dominio).
class TareasViewModel(
    obtenerTareasUseCase: ObtenerTareasUseCase,
    private val agregarTareaUseCase: AgregarTareaUseCase,
    private val alternarEstadoTareaUseCase: AlternarEstadoTareaUseCase
) : ViewModel() {

    /**
     * Convertimos el Flow (asíncrono) en un StateFlow (observable).
     * Usamos 'viewModelScope' para que la observación se corte cuando se cierra la pantalla,
     * así no desperdiciamos recursos del dispositivo.
     *
     * started = SharingStarted.WhileSubscribed(5000): Es un margen de tolerancia.
     * Si el usuario rota la pantalla, la UI se recrea rápido y no hace falta
     * reiniciar la consulta a la base de datos.
     *
     * initialValue = TareasUiState.Cargando: Arrancamos mostrando que estamos cargando.
     */
    val uiState: StateFlow<TareasUiState> = obtenerTareasUseCase()
        .onStart {
            // Especificamos un retraso de 1.5 segundos a propósito para se que llegue a ver
            // el estado de carga (test TODO: Borrar).
            delay(1500L)
        }
        .map { listaTareas ->
            // Si la lista viene vacía avisamos, sino mandamos los datos.
            if (listaTareas.isEmpty()) {
                TareasUiState.Vacio
            } else {
                TareasUiState.Exito(listaTareas)
            }
        }
        .catch { excepcion ->
            // Si algo falla, lo atrapamos acá y mandamos el mensaje de error.
            emit(TareasUiState.Error(excepcion.message ?: "Ocurrió un error desconocido"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TareasUiState.Cargando
        )

    /**
     * Cuando el usuario quiere guardar una tarea nueva.
     * Pasamos las coordenadas si es que las tenemos (vienen de la navegación).
     */
    fun onAgregarTarea(titulo: String, lat: Double? = null, lon: Double? = null) {
        viewModelScope.launch {
            agregarTareaUseCase(titulo, lat, lon)
        }
    }
    // Para marcar o desmarcar una tarea.
    fun onAlternarEstado(id: Int) {
        viewModelScope.launch {
            alternarEstadoTareaUseCase(id)
        }
    }
}