package com.example.demolistatareas.domain.model

/**
 * Representa un mensaje público publicado en el tablero comunitario.
 *
 * @property id Identificador único del documento. Se inicializa vacío ya que el
 * motor de base de datos suele generarlo en el momento de la inserción.
 * @property contenido El texto del mensaje.
 * @property autorNombre Nombre de quien publica, para mostrar en la interfaz.
 * @property autorId Identificador del usuario creador, útil para aplicar reglas
 * como permitir borrar solo los mensajes propios.
 * @property fechaCreacion Marca de tiempo (Epoch timestamp) para ordenar el tablero.
 */
data class Anuncio(
    val id: String = "",
    val contenido: String,
    val autorNombre: String,
    val autorId: String,
    val fechaCreacion: Long
)