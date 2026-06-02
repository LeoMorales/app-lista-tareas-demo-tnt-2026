package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.repository.TareaRepository

/**
 * Encapsula la regla de negocio para la creación de tareas.
 */
class AgregarTareaUseCase(private val repository: TareaRepository) {
    /**
     * Se declara como 'suspend' debido a que interactúa con operaciones
     * de escritura que requieren asincronía para no bloquear la interfaz.
     */
    suspend operator fun invoke(titulo: String, lat: Double? = null, lon: Double? = null) {
        val tituloSanitizado = titulo.trim()
        if (tituloSanitizado.isNotBlank()) {
            repository.agregarTarea(tituloSanitizado, lat, lon)
        }
    }
}