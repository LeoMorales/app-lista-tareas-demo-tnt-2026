package com.example.demolistatareas.domain.usecase

import com.example.demolistatareas.domain.repository.TareaRepository

class AlternarEstadoTareaUseCase(private val repository: TareaRepository) {
    suspend operator fun invoke(id: Int) {
        repository.alternarEstado(id)
    }
}