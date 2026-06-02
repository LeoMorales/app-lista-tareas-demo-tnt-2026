package com.example.demolistatareas.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.demolistatareas.presentation.ui.components.MapaOSM
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

/**
 * Pantalla dedicada a la selección de coordenadas geográficas.
 * Incorpora la gestión de permisos en tiempo de ejecución y la consulta
 * al hardware de posicionamiento para un centrado dinámico.
 *
 * @param onUbicacionConfirmada Función lambda que se ejecuta cuando el usuario finaliza.
 * Recibe latitud y longitud, y se encarga de la navegación hacia atrás.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onUbicacionConfirmada: (Double, Double) -> Unit
) {
    // acceder al contexto: puente de comunicación entre la aplicación y el
    // sistema operativo que permite acceder a los recursos del dispositivo,
    // como leer las preferencias compartidas (SharedPreferences), acceder a
    // bases de datos, iniciar nuevas pantallas o, como en este caso, solicitar
    // el servicio de ubicación del sistema
    val context = LocalContext.current

    // Instancia del cliente de ubicación optimizado de Google Play Services.
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    // Coordenadas por defecto (Puerto Madryn, Chubut)
    var latitudActual by remember { mutableDoubleStateOf(-42.7692) }
    var longitudActual by remember { mutableDoubleStateOf(-65.0385) }

    // EstadoCargando #1: estado que controla la visibilidad del componente de espera
    var estaCargandoUbicacion by remember { mutableStateOf(true) }

    /**
     * Lanzador de contratos de actividad para la solicitud de múltiples permisos.
     * Se ejecuta de forma asíncrona y delega el control al sistema operativo.
     * Al retornar, evalúa si al menos el permiso de ubicación aproximada fue concedido
     * para proceder con la obtención de las coordenadas.
     */
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permisos ->
            val permisoPreciso = permisos[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            val permisoAproximado = permisos[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

            if (permisoPreciso || permisoAproximado) {
                try {
                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            if (location != null) {
                                latitudActual = location.latitude
                                longitudActual = location.longitude
                            }
                            // EstadoCargando #2. Si fue exitoso: Se libera la interfaz
                            estaCargandoUbicacion = false
                        }.addOnFailureListener {
                            // EstadoCargando #3. Fallo del censor: Se libera la interfaz (quedará en la ubicación por defecto)
                            estaCargandoUbicacion = false
                        }
                } catch (e: SecurityException) {
                    estaCargandoUbicacion = false
                }
            } else {
                // EstadoCargando #4. Permiso denegado: Se libera la interfaz de inmediato
                estaCargandoUbicacion = false
            }
        }
    )

    /**
     * Efecto secundario disparado únicamente durante la composición inicial de la pantalla.
     * Verifica el estado actual de los permisos. Si ya fueron concedidos previamente,
     * consulta la ubicación de inmediato. En caso contrario, lanza la petición al usuario.
     * LaunchedEffect(Unit) asegura que esta validación se ejecute una sola vez al ingresar al mapa.
     */
    LaunchedEffect(Unit) {
        val tienePermisoPreciso = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val tienePermisoAproximado = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (tienePermisoPreciso || tienePermisoAproximado) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { location ->
                    if (location != null) {
                        latitudActual = location.latitude
                        longitudActual = location.longitude
                    }
                    estaCargandoUbicacion = false
                }
                .addOnFailureListener {
                    estaCargandoUbicacion = false
                }
        } else {
            // Si no hay permisos, el launcher se encarga, pero la pantalla debe
            // seguir mostrando el indicador de carga hasta que el usuario decida.
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Seleccionar Ubicación") })
        },
        floatingActionButton = {
            // Se deshabilita visual y funcionalmente el botón si el mapa aún no está listo
            if (!estaCargandoUbicacion) {
                // Botón de confirmación que dispara el evento de retorno
                ExtendedFloatingActionButton(
                    onClick = { onUbicacionConfirmada(latitudActual, longitudActual) },
                    icon = { Icon(Icons.Default.Check, contentDescription = "Confirmar") },
                    text = { Text("Confirmar Ubicación") }
                )
            }
        }
    ) { paddingValues ->
        // EstadoCargando #5. Bloquea la instanciación del mapa hasta resolver la ubicación
        if (estaCargandoUbicacion) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            MapaOSM(
                latitud = latitudActual,
                longitud = longitudActual,
                onUbicacionSeleccionada = { nuevaLat, nuevaLon ->
                    // Se actualiza el estado local cada vez que el usuario interactúa
                    latitudActual = nuevaLat
                    longitudActual = nuevaLon
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        }
    }
}