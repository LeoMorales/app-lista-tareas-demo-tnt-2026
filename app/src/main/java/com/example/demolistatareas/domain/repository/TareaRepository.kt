package com.example.demolistatareas.domain.repository

import com.example.demolistatareas.domain.model.Tarea
import kotlinx.coroutines.flow.Flow

/**
 * Es el contrato de nuestra arquitectura. Acá definimos qué acciones puede hacer el negocio con las tareas,
 * sin importar cómo se guarden los datos (Inversión de Dependencias).
 */
interface TareaRepository {
    fun observarTareas(): Flow<List<Tarea>>
    suspend fun agregarTarea(titulo: String, lat: Double? = null, lon: Double? = null)
    suspend fun alternarEstado(id: Int)
}