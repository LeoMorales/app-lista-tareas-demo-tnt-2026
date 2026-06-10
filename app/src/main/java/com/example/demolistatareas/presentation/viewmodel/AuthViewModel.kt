package com.example.demolistatareas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demolistatareas.domain.model.Usuario
import com.example.demolistatareas.domain.repository.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel encargado de exponer el estado de la sesión actual hacia la interfaz.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    suspend fun iniciarSesion(email: String, clave: String): Result<Usuario> {
        return authRepository.iniciarSesion(email, clave)
    }

    /**
     * Operación stateIn():
     * Transforma un Flow "frío" (que solo emite si alguien escucha) en un StateFlow "caliente"
     * (que retiene el último valor en memoria).
     * * WhileSubscribed(5000): Si la app pasa a segundo plano y la vista deja de observar,
     * el ViewModel espera 5 segundos antes de cancelar la suscripción a Firebase,
     * ahorrando batería y datos móviles.
     */
    val usuarioActual: StateFlow<Usuario?> = authRepository.observarUsuarioActual()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    /**
     * Procesa el inicio de sesión con servicio externo una vez que la interfaz
     * gráfica ha obtenido el token de identidad desde el sistema operativo.
     */
    suspend fun iniciarSesionConGoogle(idToken: String): Result<Usuario> {
        return authRepository.iniciarSesionConGoogle(idToken)
    }

    fun cerrarSesion() {
        // viewModelScope asegura que la corrutina se cancele si el ViewModel es destruido
        viewModelScope.launch {
            authRepository.cerrarSesion()
        }
    }
}