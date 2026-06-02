package com.example.demolistatareas.data.repository

import com.example.demolistatareas.data.remote.api.JsonPlaceholderApi
import com.example.demolistatareas.data.remote.dto.toDomain
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.domain.repository.TareaLaboralRepository

// Implementación para manejar los datos de la web. Se encarga de llamar a la API y transformar los resultados a objetos de dominio.
class TareaLaboralRepositoryImpl(
    private val api: JsonPlaceholderApi
) : TareaLaboralRepository {

    override suspend fun obtenerTareasSugeridas(): List<Tarea> {
        val respuestaRed = api.obtenerTareas()

        // Transformamos cada elemento DTO en un objeto de Dominio
        return respuestaRed.map { dto ->
            dto.toDomain()
        }
    }
}