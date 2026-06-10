package com.example.demolistatareas.presentation.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.demolistatareas.domain.model.Usuario
import com.example.demolistatareas.presentation.state.TareasUiState
import com.example.demolistatareas.presentation.ui.LoginScreen
import com.example.demolistatareas.presentation.ui.MapaGlobalScreen
import com.example.demolistatareas.presentation.ui.MapaScreen
import com.example.demolistatareas.presentation.ui.TableroScreen
import com.example.demolistatareas.presentation.ui.TareasLaboralesScreen
import com.example.demolistatareas.presentation.ui.TareasScreen
import com.example.demolistatareas.presentation.viewmodel.AuthViewModel
import com.example.demolistatareas.presentation.viewmodel.TableroViewModel
import com.example.demolistatareas.presentation.viewmodel.TareasLaboralesViewModel
import com.example.demolistatareas.presentation.viewmodel.TareasViewModel
import kotlinx.coroutines.launch

sealed class DrawerDestinations(val route: String, val icon: ImageVector, val label: String) {
    object Tablero : DrawerDestinations("tablero", Icons.Default.Home, "Tablero")
    object TareasLocales : DrawerDestinations("tareas_locales", Icons.AutoMirrored.Filled.List, "Mis Tareas")
    object TareasLaborales : DrawerDestinations("tareas_laborales", Icons.Default.Work, "Tareas Laborales")
    object MapaGlobal : DrawerDestinations("mapa_global", Icons.Default.LocationOn, "Mapa Global")
}

/**
* Orquestador de la navegación de la aplicación (Router).
* Define las rutas existentes y qué Composable renderizar en cada una.
* Aislar esta responsabilidad facilita la lectura de la Activity principal.
*/
@Composable
fun AppNavigation(
    viewModelFactory: ViewModelProvider.Factory
) {
    val authViewModel: AuthViewModel = viewModel(factory = viewModelFactory)
    val tableroViewModel: TableroViewModel = viewModel(factory = viewModelFactory)

    val navController = rememberNavController()
    val usuarioActual by authViewModel.usuarioActual.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val items = listOf(
        DrawerDestinations.Tablero,
        DrawerDestinations.TareasLocales,
        DrawerDestinations.TareasLaborales,
        DrawerDestinations.MapaGlobal
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Solo mostramos el Drawer si hay un usuario logueado
    if (usuarioActual != null) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Lista de Tareas TNT 2026",
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    items.forEach { item ->
                        NavigationDrawerItem(
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                        )
                    }
                }
            }
        ) {
            AppNavHost(
                navController = navController,
                usuarioActual = usuarioActual,
                authViewModel = authViewModel,
                tableroViewModel = tableroViewModel,
                viewModelFactory = viewModelFactory,
                onOpenDrawer = { scope.launch { drawerState.open() } }
            )
        }
    } else {
        // Si no hay usuario, mostramos solo el NavHost (que llevará al login)
        AppNavHost(
            navController = navController,
            usuarioActual = usuarioActual,
            authViewModel = authViewModel,
            tableroViewModel = tableroViewModel,
            viewModelFactory = viewModelFactory,
            onOpenDrawer = {}
        )
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    usuarioActual: Usuario?,
    authViewModel: AuthViewModel,
    tableroViewModel: TableroViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    onOpenDrawer: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (usuarioActual == null) "login" else "tablero"
    ) {

        composable("login") {
            LoginScreen(
                authViewModel,
                onLoginExitoso = {
                    navController.navigate("tablero") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("tablero") {
            usuarioActual?.let { usuario ->
                TableroScreen(
                    viewModel = tableroViewModel,
                    usuarioActual = usuario,
                    onOpenDrawer = onOpenDrawer
                )
            }
        }
        composable("tareas_locales") { backStackEntry ->
            val viewModel: TareasViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsState()

            val savedStateHandle = backStackEntry.savedStateHandle
            val latitudRetorno by savedStateHandle
                .getStateFlow<Double?>(
                    "lat",
                    null
                ).collectAsState()
            val longitudRetorno by savedStateHandle
                .getStateFlow<Double?>("lon", null)
                .collectAsState()

            TareasScreen(
                uiState = uiState,
                latitudSeleccionada = latitudRetorno,
                longitudSeleccionada = longitudRetorno,
                onAgregarTarea = { titulo ->
                    viewModel.onAgregarTarea(
                        titulo = titulo,
                        lat = latitudRetorno,
                        lon = longitudRetorno
                    )
                    savedStateHandle.remove<Double>("lat")
                    savedStateHandle.remove<Double>("lon")
                },
                onAlternarEstado = viewModel::onAlternarEstado,
                onNavegarALaborales = { navController.navigate("tareas_laborales") },
                onAbrirMapa = { navController.navigate("seleccion_mapa") },
                onVerMapaGlobal = { navController.navigate("mapa_global") },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable("tareas_laborales") {
            val viewModel: TareasLaboralesViewModel = viewModel(
                factory = viewModelFactory
            )
            val uiState by viewModel.uiState.collectAsState()

            TareasLaboralesScreen(
                uiState = uiState,
                onVolver = { navController.popBackStack() },
                onOpenDrawer = onOpenDrawer
            )
        }

        composable("seleccion_mapa") {
            MapaScreen(
                onUbicacionConfirmada = { lat, lon ->
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set("lat", lat)
                        set("lon", lon)
                    }
                    navController.popBackStack()
                }
            )
        }

        composable("mapa_global") {
            val viewModel: TareasViewModel = viewModel(factory = viewModelFactory)
            val uiState by viewModel.uiState.collectAsState()

            if (uiState is TareasUiState.Exito) {
                MapaGlobalScreen(
                    tareas = (uiState as TareasUiState.Exito).tareas,
                    onVolver = { navController.popBackStack() },
                    onOpenDrawer = onOpenDrawer
                )
            }
        }
    }
}
