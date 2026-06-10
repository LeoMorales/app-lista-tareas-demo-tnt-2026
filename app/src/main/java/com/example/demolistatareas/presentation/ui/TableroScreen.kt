package com.example.demolistatareas.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import com.example.demolistatareas.domain.model.Usuario
import com.example.demolistatareas.presentation.viewmodel.TableroViewModel

/**
 * Interfaz visual interactiva para el tablero comunitario.
 * Se divide estructuralmente en una zona de lectura (lista de mensajes)
 * y una zona de escritura (formulario de ingreso).
 *
 * @param viewModel Orquestador del estado de la pantalla.
 * @param usuarioActual Entidad del usuario que tiene la sesión activa,
 * requerida para firmar las nuevas publicaciones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TableroScreen(
    viewModel: TableroViewModel,
    usuarioActual: Usuario,
    onOpenDrawer: () -> Unit
) {
    // La directiva collectAsStateWithLifecycle asegura que la interfaz detenga la
    // recolección de datos si la aplicación pasa a segundo plano, optimizando
    // el consumo de memoria y CPU.
    val anuncios by viewModel.listaAnuncios.collectAsStateWithLifecycle()
    val textoActual by viewModel.textoPublicacion.collectAsStateWithLifecycle()
    val estaPublicando by viewModel.estaPublicando.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tablero Comunitario") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 1. Zona de Lectura: Renderizado reactivo de la colección
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(anuncios) { anuncio ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = anuncio.autorNombre,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = anuncio.contenido,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // 2. Zona de Escritura: Ingreso de datos y confirmación
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textoActual,
                    onValueChange = { viewModel.actualizarTexto(it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Escribir mensaje comunitario...") },
                    enabled = !estaPublicando // Se bloquea la edición durante peticiones de red
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { viewModel.publicarMensaje(usuarioActual) },
                    enabled = textoActual.isNotBlank() && !estaPublicando
                ) {
                    // Intercambio de componente visual para retroalimentación de estado
                    if (estaPublicando) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Publicar anuncio")
                    }
                }
            }
        }
    }
}
