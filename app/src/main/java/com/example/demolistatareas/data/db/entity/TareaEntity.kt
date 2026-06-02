package com.example.demolistatareas.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.demolistatareas.domain.model.Coordenada
import com.example.demolistatareas.domain.model.Tarea

/**
 * Representación de la tabla de tareas en la base de datos SQLite.
 * Esta clase pertenece estrictamente a la capa de infraestructura.
 */
@Entity(tableName = "tareas")
data class TareaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val titulo: String,
    val estaCompletada: Boolean,
    val latitud: Double? = null,
    val longitud: Double? = null
)

/**
 * Funciones de extensión (Mappers) para traducir entre modelos de infraestructura y dominio.
 * Aisla a las entidades de negocio de las librerías de persistencia.
 * Reconstruye el objeto semántico [Coordenada] únicamente si ambas columnas
 * poseen valores válidos en la base de datos.
 */
fun TareaEntity.toDomain(): Tarea {
    val ubicacion = if (latitud != null && longitud != null) {
        Coordenada(latitud, longitud)
    } else null

    return Tarea(
        id = this.id,
        titulo = this.titulo,
        estaCompletada = this.estaCompletada,
        ubicacion = ubicacion
    )
}

/**
 * Función de mapeo hacia la Infraestructura.
 * Desacopla las propiedades del objeto [Coordenada] en tipos primitivos
 * para su correcto almacenamiento en SQLite.
 */
fun Tarea.toEntity(): TareaEntity {
    return TareaEntity(
        id = this.id,
        titulo = this.titulo,
        estaCompletada = this.estaCompletada,
        latitud = this.ubicacion?.latitud,
        longitud = this.ubicacion?.longitud
    )
}