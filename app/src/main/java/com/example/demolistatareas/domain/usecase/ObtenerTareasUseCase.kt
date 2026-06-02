package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow

// Se encarga de traer el flujo de tareas actualizado.
class ObtenerTareasUseCase(private val repository: TareaRepository) {
    // Usamos 'invoke' para poder llamar al caso de uso directamente como si fuera una función, simplificando el código.
    operator fun invoke(): Flow<List<Tarea>> {
        return repository.observarTareas()
    }
}