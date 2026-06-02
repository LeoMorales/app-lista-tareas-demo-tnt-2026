package com.example.demolistatareas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demolistatareas.domain.usecase.ObtenerTareasSugeridasUseCase
import com.example.demolistatareas.presentation.state.TareasLaboralesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gestiona el ciclo de vida de la petición de red y el estado de la vista asociada.
 */
class TareasLaboralesViewModel(
    private val obtenerTareasUseCase: ObtenerTareasSugeridasUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<TareasLaboralesUiState>(TareasLaboralesUiState.Cargando)
    val uiState: StateFlow<TareasLaboralesUiState> = _uiState.asStateFlow()

    init {
        cargarTareas()
    }

    private fun cargarTareas() {
        viewModelScope.launch {
            _uiState.value = TareasLaboralesUiState.Cargando
            try {
                // Se invoca la operación asíncrona suspendiendo la ejecución local
                // hasta obtener la respuesta del servidor.
                val tareas = obtenerTareasUseCase()
                _uiState.value = TareasLaboralesUiState.Exito(tareas)
            } catch (e: Exception) {
                // Se interceptan fallos de red (ej. UnknownHostException) o parseo
                _uiState.value = TareasLaboralesUiState.Error(
                    mensaje = e.message ?: "Ocurrió un error al contactar el servidor"
                )
            }
        }
    }
}