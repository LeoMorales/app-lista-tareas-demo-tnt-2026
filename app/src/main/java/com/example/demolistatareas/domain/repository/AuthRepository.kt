package com.example.demolistatareas.domain.repository

import com.example.demolistatareas.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Contrato para la gestión de identidad y sesiones.
 */
interface AuthRepository {
    /**
     * Emite un flujo continuo con el estado de la sesión.
     * Si no hay sesión activa, emite null.
     */
    fun observarUsuarioActual(): Flow<Usuario?>

    /**
     * Intenta autenticar a un usuario mediante credenciales tradicionales.
     * Se utiliza la clase Result de Kotlin para encapsular éxitos o fracasos
     * de manera segura, evitando el lanzamiento descontrolado de excepciones.
     */
    suspend fun iniciarSesion(email: String, clave: String): Result<Usuario>

    /**
     * Autentica al usuario en el sistema utilizando un token de identidad provisto por Google.
     * * @param idToken Cadena de texto criptográfica y firmada que demuestra la identidad
     * del usuario ante los servidores de autenticación.
     * @return Un objeto [Result] que encapsula el perfil del [Usuario] en caso de éxito
     * o la excepción correspondiente en caso de fallo de red o validación.
     */
    suspend fun iniciarSesionConGoogle(idToken: String): Result<Usuario>

    /**
     * Destruye la sesión actual en el dispositivo.
     */
    suspend fun cerrarSesion()
}