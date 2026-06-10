package com.example.demolistatareas.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.demolistatareas.domain.model.Anuncio
import com.example.demolistatareas.domain.model.Usuario
import com.example.demolistatareas.domain.usecase.ObtenerAnunciosUseCase
import com.example.demolistatareas.domain.usecase.PublicarAnuncioUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Orquesta la visualización de mensajes y la lógica del formulario de publicación.
 */
class TableroViewModel(
    private val obtenerAnunciosUseCase: ObtenerAnunciosUseCase,
    private val publicarAnuncioUseCase: PublicarAnuncioUseCase
) : ViewModel() {

    // 1. Estado de Lectura (Flujo que viene desde Firestore a través del Dominio)
    val listaAnuncios: StateFlow<List<Anuncio>> = obtenerAnunciosUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // 2. Estado del Formulario (Controlado localmente por la vista)
    private val _textoPublicacion = MutableStateFlow("")
    val textoPublicacion = _textoPublicacion.asStateFlow()

    // 3. Estado de Carga de red para feedback visual (ej. deshabilitar botón al enviar)
    private val _estaPublicando = MutableStateFlow(false)
    val estaPublicando = _estaPublicando.asStateFlow()

    fun actualizarTexto(nuevoTexto: String) {
        _textoPublicacion.value = nuevoTexto
    }

    /**
     * Ejecuta el intento de publicación. Requiere recibir el usuario autenticado
     * como parámetro desde la vista, garantizando que todo anuncio tenga un autor válido.
     */
    fun publicarMensaje(autor: Usuario) {
        val textoActual = _textoPublicacion.value

        // Validación preventiva en presentación (evita carga inútil en la red)
        if (textoActual.isBlank()) return

        _estaPublicando.value = true

        viewModelScope.launch {
            val nuevoAnuncio = Anuncio(
                contenido = textoActual,
                autorNombre = autor.nombre,
                autorId = autor.id,
                fechaCreacion = System.currentTimeMillis()
            )

            val resultado = publicarAnuncioUseCase(nuevoAnuncio)

            if (resultado.isSuccess) {
                // Si la red confirma la inserción, limpiamos el formulario.
                // Automáticamente Firestore emitirá un nuevo flujo y 'listaAnuncios' se actualizará.
                _textoPublicacion.value = ""
            } else {
                // Acá se podría derivar el error a un estado de Snackbar/Toast para notificar al usuario.
            }

            _estaPublicando.value = false
        }
    }
}