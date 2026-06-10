package com.example.demolistatareas.domain.model

/**
 * Representa a un usuario autenticado en el sistema.
 * Es una entidad plana que aísla la lógica de negocio de los objetos complejos
 * que devuelven los proveedores de identidad (como Google o Firebase).
 *
 * @property id Identificador único y universal del usuario.
 * @property nombre Nombre visible del usuario.
 * @property email Correo electrónico utilizado para la autenticación.
 */
data class Usuario(
    val id: String,
    val nombre: String,
    val email: String
)