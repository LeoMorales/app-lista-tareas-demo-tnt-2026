package com.example.demolistatareas.domain.model

/**
 * Representa una coordenada geográfica opcional asociada a una entidad del sistema.
 * Se utiliza una estructura inmutable para garantizar que las coordenadas
 * no sean alteradas de forma colateral por componentes externos.
 *
 * @property latitud Valor de la coordenada latitudinal (e.je., -42.7692).
 * @property longitud Valor de la coordenada longitudinal (e.je., -65.0385).
 */
data class Coordenada(
    val latitud: Double,
    val longitud: Double
)

/**
 * Entidad fundamental que representa una unidad de trabajo en el sistema.
 * Se utiliza una 'data class' inmutable (val) para garantizar la integridad
 * de los datos. Ningún componente externo puede modificar una instancia
 * existente; se debe crear una copia con el nuevo estado.
 */
data class Tarea(
    val id: Int,
    val titulo: String,
    val estaCompletada: Boolean = false,
    val ubicacion: Coordenada? = null // Se declara como nullable para mantener compatibilidad con tareas previas
)