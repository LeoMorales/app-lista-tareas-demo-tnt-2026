package com.example.demolistatareas.data.repository

import com.example.demolistatareas.domain.model.Coordenada
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Implementación concreta del contrato definido por el dominio.
 * Mantiene los datos en memoria volátil (RAM).
 * Esta estructura simula la reactividad de una base de datos local (como Room),
 * permitiendo una transición transparente en el futuro.
 */
class TareaRepositoryInMemoryImpl : TareaRepository {

    // MutableStateFlow actúa como el contenedor reactivo privado de la lista.
    private val _tareasFlow = MutableStateFlow<List<Tarea>>(emptyList())
    private var contadorIds = 1

    override fun observarTareas(): Flow<List<Tarea>> {
        // Se expone una versión de solo lectura hacia el exterior
        return _tareasFlow.asStateFlow()
    }

    override suspend fun agregarTarea(titulo: String, lat: Double?, lon: Double?) {

        val ubicacion = if (lat != null && lon != null) {
            Coordenada(lat, lon)
        } else null

        val nuevaTarea = Tarea(
            id = contadorIds++,
            titulo = titulo,
            ubicacion = ubicacion
        )
        // update modifica el valor actual de forma atómica y segura
        _tareasFlow.update { listaActual -> listaActual + nuevaTarea }
    }

    override suspend fun alternarEstado(id: Int) {
        _tareasFlow.update { listaActual ->
            listaActual.map { tarea ->
                if (tarea.id == id) tarea.copy(estaCompletada = !tarea.estaCompletada)
                else tarea
            }
        }
    }
}