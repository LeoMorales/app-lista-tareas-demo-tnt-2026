package com.example.demolistatareas.presentation.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

/**
 * Componente puente que permite integrar el motor de mapas clásico (OSMdroid)
 * dentro del ecosistema declarativo de Jetpack Compose.
 *
 * @param latitud Coordenada del eje Y (Norte/Sur).
 * @param longitud Coordenada del eje X (Este/Oeste).
 * @param onUbicacionSeleccionada Función lambda que se emite cuando el usuario
 * confirma una nueva ubicación en el mapa.
 * @param modifier Modificador estándar para gestionar el tamaño y diseño del contenedor.
 */
@Composable
fun MapaOSM(
    latitud: Double,
    longitud: Double,
    onUbicacionSeleccionada: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Inicialización global requerida por OSMdroid para gestionar la caché de red.
    // Se utiliza el identificador del paquete de la aplicación para aislar los datos.
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
    )

    // Se instancia la vista tradicional (clásica de Android) y se memoriza
    // con el remember para evitar que se reconstruya íntegramente ante cada
    // cambio de estado.
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // Proveedor de imágenes públicas
            setMultiTouchControls(true)             // Habilita el zoom táctil (gesto con dos dedos)
            controller.setZoom(15.0)                // Evita que se reinicie si el usuario hace zoom manual y luego coloca un marcador.
        }
    }

    // AndroidView es el contenedor oficial que permite inyectar vistas clásicas xml.
    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // Este bloque es el puente entre el mundo declarativo y el imperativo.
            // Se ejecuta cada vez que Jetpack Compose detecta
            // un cambio en los parámetros de entrada (latitud o longitud).

            val puntoGeo = GeoPoint(latitud, longitud)
            //view.controller.setZoom(15.0)
            view.controller.setCenter(puntoGeo)

            // 1. Limpieza total de capas previas (marcadores y gestores de eventos)
            view.overlays.clear()

            // 2. Configuración del receptor de eventos táctiles
            val receptorEventos = object : MapEventsReceiver {
                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                    return false // No se intercepta el toque simple
                }

                override fun longPressHelper(p: GeoPoint): Boolean {
                    // Al detectar pulsación larga, se emiten las nuevas coordenadas hacia arriba
                    onUbicacionSeleccionada(p.latitude, p.longitude)
                    return true
                }
            }

            // Se añade la capa de eventos al mapa
            view.overlays.add(MapEventsOverlay(receptorEventos))

            // 3. Dibujado del marcador visual en la posición actual
            val marcador = Marker(view).apply {
                position = puntoGeo
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                title = "Ubicación seleccionada"
            }
            view.overlays.add(marcador)


            view.invalidate() // Fuerza el redibujado de la vista clásica
        }
    )

    // Gestión del ciclo de vida: asegura que el mapa libere recursos de memoria
    // cuando la pantalla Composable es destruida o removida del árbol visual.
    DisposableEffect(Unit) {
        onDispose {
            mapView.onDetach()
        }
    }
}