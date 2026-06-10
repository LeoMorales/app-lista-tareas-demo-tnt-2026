package com.example.demolistatareas.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.presentation.ui.components.MapaVisorOSM

// Definición de los estados posibles para nuestro filtro
enum class FiltroMapa { TODAS, COMPLETADAS, PENDIENTES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaGlobalScreen(
    tareas: List<Tarea>,
    onVolver: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    var filtroActual by remember { mutableStateOf(FiltroMapa.TODAS) }

    // Cálculo reactivo: Se recalcula SOLO si 'tareas' o 'filtroActual' cambian
    val tareasFiltradas by remember(filtroActual, tareas) {
        derivedStateOf {
            when (filtroActual) {
                FiltroMapa.TODAS -> tareas
                FiltroMapa.COMPLETADAS -> tareas.filter { it.estaCompletada }
                FiltroMapa.PENDIENTES -> tareas.filter { !it.estaCompletada }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Tareas") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                },
                actions = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            // 1. SUPERFICIE Y PRIMER PLANO
            // Envolvemos el Row en un Surface para darle un fondo sólido y despegable.
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(1f), // Fuerza este componente a la capa superior (eje Z)
                shadowElevation = 6.dp // Añade una sombra sutil que separa visualmente los filtros del mapa
            ) {
                // Fila de controles para los filtros (Material 3 FilterChips)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroActual == FiltroMapa.TODAS,
                        onClick = { filtroActual = FiltroMapa.TODAS },
                        label = { Text("Todas") }
                    )
                    FilterChip(
                        selected = filtroActual == FiltroMapa.PENDIENTES,
                        onClick = { filtroActual = FiltroMapa.PENDIENTES },
                        label = { Text("Pendientes") }
                    )
                    FilterChip(
                        selected = filtroActual == FiltroMapa.COMPLETADAS,
                        onClick = { filtroActual = FiltroMapa.COMPLETADAS },
                        label = { Text("Completadas") }
                    )
                }
            }

            // 2. MAPA CON RECORTES ESTRICTOS
            MapaVisorOSM(
                tareas = tareasFiltradas,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds() // Prohíbe al mapa dibujar fuera de su área asignada
            )
        }
    }
}