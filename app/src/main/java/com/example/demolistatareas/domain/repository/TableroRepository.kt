package com.example.demolistatareas.domain.repository

import com.example.demolistatareas.domain.model.Anuncio
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para la interacción con la base de datos comunitaria.
 */
interface TableroRepository {
    /**
     * Abre un canal de comunicación reactivo. Cualquier cambio en la fuente
     * de datos original emitirá una nueva lista automáticamente.
     */
    fun obtenerAnuncios(): Flow<List<Anuncio>>

    /**
     * Persiste un nuevo anuncio.
     */
    suspend fun publicarAnuncio(anuncio: Anuncio): Result<Unit>
}