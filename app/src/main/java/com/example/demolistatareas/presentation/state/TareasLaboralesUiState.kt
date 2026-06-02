package com.example.demolistatareas.presentation.state

import com.example.demolistatareas.domain.model.Tarea

// Representa los posibles estados de la pantalla de tareas. Solo puede estar en uno a la vez.
sealed interface TareasLaboralesUiState {
    data object Cargando : TareasLaboralesUiState
    data class Exito(val tareas: List<Tarea>) : TareasLaboralesUiState
    data class Error(val mensaje: String) : TareasLaboralesUiState
}