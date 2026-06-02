package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.repository.TareaLaboralRepository

/**
 * Encapsula la regla de aplicación responsable de solicitar sugerencias externas.
 */
class ObtenerTareasSugeridasUseCase(
    private val repository: TareaLaboralRepository
) {
    suspend operator fun invoke(): List<Tarea> {
        return repository.obtenerTareasSugeridas()
    }
}