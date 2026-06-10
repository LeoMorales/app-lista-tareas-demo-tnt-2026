package com.example.demolistatareas.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.presentation.state.TareasUiState
import com.example.demolistatareas.presentation.ui.components.ItemTarea

/**
 * Pantalla principal. Sigue el principio de Flujo Unidireccional de Datos (UDF).
 * La interfaz es pasiva: recibe la lista de tareas para dibujar y emite
 * eventos de interacción mediante lambdas (onAgregarTarea, onAlternarEstado).
 *
 * @param modifier Permite al componente contenedor ajustar el layout de esta pantalla.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareasScreen(
    uiState: TareasUiState,
    latitudSeleccionada: Double?,
    longitudSeleccionada: Double?,
    onAgregarTarea: (String) -> Unit,
    onAlternarEstado: (Int) -> Unit,
    onNavegarALaborales: () -> Unit,
    onAbrirMapa: () -> Unit,
    modifier: Modifier = Modifier,
    onVerMapaGlobal: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    var textoInput by rememberSaveable { mutableStateOf("") }

    // Calculamos si debemos mostrar el botón del mapa global
    val tieneTareasConUbicacion = remember(uiState) {
        (uiState as? TareasUiState.Exito)?.tareas?.any { it.ubicacion != null } == true
    }

    // El Scaffold asume la raíz visual. Se le delega el modificador principal.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis Tareas Locales") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(
                // Alineamos a la derecha para que los centros de los botones coincidan
                horizontalAlignment = Alignment.End,
                // Agregamos una separación estándar entre los botones
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón secundario para el mapa: solo se muestra si hay georeferencias
                if (tieneTareasConUbicacion) {
                    SmallFloatingActionButton(
                        onClick = onVerMapaGlobal,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Ver mapa global")
                    }
                }

                // Botón principal
                FloatingActionButton(
                    onClick = onNavegarALaborales,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Ver tareas laborales"
                    )
                }
            }
        }
    ) { paddingValues ->

        // Se aplica el paddingValues provisto por el Scaffold al contenedor interno.
        // Esto previene la colisión o superposición visual con el botón flotante.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // El componente de ingreso de datos se mantiene constante independientemente del estado
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = textoInput,
                    onValueChange = { textoInput = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Nueva tarea") }
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Botón dedicado a iniciar el flujo de georeferenciación
                // El IconButton provee el "touch target" estándar y captura la intención de navegación.
                IconButton(
                    onClick = onAbrirMapa
                ) {
                    // Se altera el ícono y el color si ya existen coordenadas en el "buzón"
                    if (latitudSeleccionada != null && longitudSeleccionada != null) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ubicación asignada",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = "Asignar ubicación",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        onAgregarTarea(textoInput)
                        textoInput = ""
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Añadir")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bloque de renderizado condicional exhaustivo
            when (uiState) {
                is TareasUiState.Cargando -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is TareasUiState.Vacio -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No hay tareas pendientes. Agregá una nueva!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                is TareasUiState.Exito -> {
                    ListaTareas(
                        tareas = uiState.tareas,
                        onAlternarEstado = onAlternarEstado
                    )
                }

                is TareasUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Error: ${uiState.mensaje}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

/**
 * Se extrae la representación de la lista a un componente independiente
 * para mantener la claridad y modularidad del código visual.
 */
@Composable
private fun ListaTareas(
    tareas: List<Tarea>,
    onAlternarEstado: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(tareas) { tarea ->
            ItemTarea(
                tarea = tarea,
                onAlternarEstado = { onAlternarEstado(tarea.id) }
            )

        }
    }
}