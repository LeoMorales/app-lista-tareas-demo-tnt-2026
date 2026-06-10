package com.example.demolistatareas.data.repository

import com.example.demolistatareas.domain.model.Usuario
import com.example.demolistatareas.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementación concreta del repositorio de autenticación utilizando Firebase Auth.
 * Esta clase pertenece a la Capa de Datos y es la única que interactúa con el SDK de Firebase.
 *
 * @param firebaseAuth Instancia del servicio de autenticación de Firebase.
 */
class AuthRepositoryFirebaseImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    /**
     * Transforma el modelo interno de Firebase (FirebaseUser) a la entidad pura del Dominio (Usuario).
     * En caso de que el usuario no posea nombre registrado, se asigna un valor por defecto
     * basado en su correo electrónico para facilitar la legibilidad en la interfaz.
     */
    private fun mapearUsuario(firebaseUser: com.google.firebase.auth.FirebaseUser?): Usuario? {
        if (firebaseUser == null) return null

        val nombreAlternativo = firebaseUser.email?.substringBefore("@") ?: "Usuario"

        return Usuario(
            id = firebaseUser.uid,
            nombre = firebaseUser.displayName ?: nombreAlternativo,
            email = firebaseUser.email ?: ""
        )
    }

    override fun observarUsuarioActual(): Flow<Usuario?> = callbackFlow {
        // 1. Se define un "oyente" que Firebase invocará cada vez que la sesión cambie
        // (ej. al iniciar sesión, cerrar sesión o si expira el token de seguridad).
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            // trySend emite el nuevo estado hacia el flujo reactivo de manera segura
            trySend(mapearUsuario(auth.currentUser))
        }

        // 2. Se inscribe el oyente en el ciclo de vida de Firebase
        firebaseAuth.addAuthStateListener(authStateListener)

        // 3. Gestión de memoria: awaitClose suspende la ejecución de esta corrutina
        // hasta que el consumidor del flujo (ej. ViewModel) es destruido o cancelado.
        // Es imperativo remover el oyente en este bloque para evitar fugas de memoria (memory leaks).
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    override suspend fun iniciarSesion(email: String, clave: String): Result<Usuario> {
        return try {
            // La función de extensión .await() suspende la corrutina hasta que la tarea
            // asíncrona de Firebase finalice, previniendo el uso de callbacks anidados (Callback Hell).
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, clave).await()
            val usuario = mapearUsuario(authResult.user)

            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Error interno: No se pudo mapear el perfil de usuario tras la autenticación."))
            }
        } catch (e: Exception) {
            // Se capturan las excepciones propias de Firebase (ej. FirebaseAuthInvalidCredentialsException)
            // y se devuelven envueltas en el patrón Result, protegiendo a las capas superiores.
            Result.failure(e)
        }
    }

    override suspend fun iniciarSesionConGoogle(idToken: String): Result<Usuario> {
        return try {
            // 1. Se genera un objeto de credencial específico para el proveedor Google
            // utilizando el token de identidad recibido desde la interfaz de usuario.
            val credencial = GoogleAuthProvider.getCredential(idToken, null)

            // 2. Se delega la autenticación a Firebase pasando la credencial generada.
            // La función de extensión .await() suspende la ejecución de forma no bloqueante
            // hasta que el servidor confirme la validez del token.
            val resultadoAuth = firebaseAuth.signInWithCredential(credencial).await()

            // 3. Se reutiliza el mapeador existente para estandarizar el perfil del usuario.
            val usuario = mapearUsuario(resultadoAuth.user)

            if (usuario != null) {
                Result.success(usuario)
            } else {
                Result.failure(Exception("Error en el mapeo de datos tras el inicio de sesión federado."))
            }
        } catch (e: Exception) {
            // Captura errores comunes como tokens expirados, falta de conexión o cancelaciones de red
            Result.failure(e)
        }
    }

    override suspend fun cerrarSesion() {
        // La API de Firebase gestiona el borrado de credenciales locales de forma síncrona.
        firebaseAuth.signOut()
    }
}