package com.example.demolistatareas.presentation.state

import com.example.demolistatareas.domain.model.Tarea

/**
 * Representa los estados de la pantalla de tareas. Usamos una 'sealed interface'
 * para que el compilador nos obligue a manejar todos los casos posibles,
 * evitando estados inconsistentes.
 */
sealed interface TareasUiState {

    /**
     * La app está buscando los datos. Usamos 'data object' porque no
     * necesitamos guardar información extra en este estado.
     */
    data object Cargando : TareasUiState

    /**
     * La búsqueda terminó bien pero no hay ninguna tarea para mostrar.
     */
    data object Vacio : TareasUiState

    /**
     * Tenemos las tareas listas para mostrar en la pantalla.
     */
    data class Exito(val tareas: List<Tarea>) : TareasUiState

    /**
     * Hubo un problema al traer los datos. Guardamos el mensaje para avisarle al usuario.
     */
    data class Error(val mensaje: String) : TareasUiState
}