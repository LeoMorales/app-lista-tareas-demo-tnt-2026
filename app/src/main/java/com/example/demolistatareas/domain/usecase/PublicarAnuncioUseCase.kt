package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.model.Anuncio
import com.example.demolistatareas.domain.repository.TableroRepository

/**
 * Caso de uso responsable de validar y emitir un nuevo anuncio.
 */
class PublicarAnuncioUseCase(
    private val tableroRepository: TableroRepository
) {
    /**
     * Aplica la validación principal: un anuncio no puede publicarse vacío.
     */
    suspend operator fun invoke(anuncio: Anuncio): Result<Unit> {
        if (anuncio.contenido.isBlank()) {
            return Result.failure(IllegalArgumentException("El contenido del anuncio no puede estar vacío"))
        }
        return tableroRepository.publicarAnuncio(anuncio)
    }
}