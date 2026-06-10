package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.model.Anuncio
import com.example.demolistatareas.domain.repository.TableroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Caso de uso responsable de suministrar la lista de anuncios.
 * Aplica reglas de negocio sobre la lectura, como garantizar un ordenamiento
 * cronológico inverso (los más recientes primero).
 */
class ObtenerAnunciosUseCase(
    private val tableroRepository: TableroRepository
) {
    operator fun invoke(): Flow<List<Anuncio>> {
        // Se intercepta el flujo del repositorio para aplicar ordenamiento
        // antes de que llegue a la capa de presentación.
        return tableroRepository.obtenerAnuncios().map { lista ->
            lista.sortedByDescending { it.fechaCreacion }
        }
    }
}