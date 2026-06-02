package com.example.demolistatareas.domain.repository

import com.example.demolistatareas.domain.model.Tarea

/**
 * Límite arquitectónico que define la obtención de tareas desde un origen externo.
 * Permanece agnóstico respecto a la tecnología subyacente (REST, GraphQL, gRPC).
 */
interface TareaLaboralRepository {
    suspend fun obtenerTareasSugeridas(): List<Tarea>
}