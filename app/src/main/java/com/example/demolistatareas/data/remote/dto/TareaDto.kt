package com.example.demolistatareas.data.remote.dto

import com.example.demolistatareas.domain.model.Tarea

/**
 * Data Transfer Object (DTO) que modela la estructura exacta del JSON
 * devuelto por la API externa (JSONPlaceholder).
 * Su existencia aísla las particularidades de la red de la capa de Dominio.
 */
data class TareaDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

/**
 * Función de extensión responsable de transformar el modelo de red en el modelo puro de negocio.
 * Garantiza que la terminología externa (ej. 'title', 'completed') se adapte a la semántica
 * interna del sistema ('titulo', 'estaCompletada').
 */
fun TareaDto.toDomain(): Tarea {
    return Tarea(
        id = this.id,
        titulo = this.title,
        estaCompletada = this.completed
    )
}