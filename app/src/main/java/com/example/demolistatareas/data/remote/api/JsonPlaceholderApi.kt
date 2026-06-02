package com.example.demolistatareas.data.remote.api

import com.example.demolistatareas.data.remote.dto.TareaDto
import retrofit2.http.GET

/**
 * Define el contrato de comunicación HTTP con el servidor remoto.
 * Retrofit generará la implementación real de esta interfaz en tiempo de ejecución.
 */
interface JsonPlaceholderApi {

    /**
     * Obtiene el listado completo de tareas desde el servidor.
     * La palabra clave 'suspend' indica que la operación de red se ejecutará de forma asíncrona.
     */
    @GET("todos")
    suspend fun obtenerTareas(): List<TareaDto>
}