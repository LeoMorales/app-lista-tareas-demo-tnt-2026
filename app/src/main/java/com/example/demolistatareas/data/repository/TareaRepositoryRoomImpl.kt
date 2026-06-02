package com.example.demolistatareas.data.repository

import com.example.demolistatareas.data.db.dao.TareaDao
import com.example.demolistatareas.data.db.entity.TareaEntity
import com.example.demolistatareas.data.db.entity.toDomain
import com.example.demolistatareas.domain.model.Coordenada
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.repository.TareaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Implementación del contrato de dominio usando Room para persistir los datos.
class TareaRepositoryRoomImpl(
    private val dao: TareaDao
) : TareaRepository {

    override fun observarTareas(): Flow<List<Tarea>> {
        // Obtenemos el flujo de entidades de Room y mapeamos cada lista a objetos de dominio.
        return dao.observarTodas().map { listaEntidades ->
            listaEntidades.map { entidad -> entidad.toDomain() }
        }
    }

    override suspend fun agregarTarea(titulo: String, lat: Double?, lon: Double?) {

        val nuevaEntidad = TareaEntity(
            titulo = titulo,
            estaCompletada = false,
            latitud = lat,
            longitud = lon
        )
        dao.insertar(nuevaEntidad)
    }

    override suspend fun alternarEstado(id: Int) {
        // Consultamos el estado actual y lo invertimos directamente con una actualización en la base.
        val estadoActual = dao.obtenerEstadoActual(id)
        dao.actualizarEstado(id, !estadoActual)
    }
}