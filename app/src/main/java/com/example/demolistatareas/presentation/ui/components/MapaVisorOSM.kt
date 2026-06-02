package com.example.demolistatareas.presentation.ui.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.demolistatareas.domain.model.Tarea
import com.example.demolistatareas.ui.theme.DemoListaTareasTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


@Composable
fun MapaVisorOSM(
    tareas: List<Tarea>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Configuration.getInstance().load(
        context,
        context.getSharedPreferences("osm_prefs", Context.MODE_PRIVATE)
    )

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            // Centramos por defecto en Puerto Madryn
            controller.setCenter(GeoPoint(-42.7692, -65.0385))
            controller.setZoom(13.0)
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            // 1. Limpiamos los marcadores anteriores antes de redibujar
            view.overlays.clear()

            // 2. Filtramos solo las tareas que tienen coordenadas asignadas
            val tareasGeoreferenciadas = tareas.filter { it.ubicacion != null }

            tareasGeoreferenciadas.forEach { tarea ->
                val puntoGeo = GeoPoint(tarea.ubicacion!!.latitud, tarea.ubicacion!!.longitud)

                val marcador = Marker(view).apply {
                    position = puntoGeo
                    title = tarea.titulo
                    snippet = if (tarea.estaCompletada) "Completada" else "Pendiente"

                    // Modificación dinámica del color del ícono nativo de OSMdroid
                    val defaultIcon = ContextCompat.getDrawable(
                        context,
                        org.osmdroid.library.R.drawable.marker_default
                    )?.mutate()

                    if (tarea.estaCompletada) {
                        defaultIcon?.setTint(android.graphics.Color.BLUE)
                    } else {
                        defaultIcon?.setTint(android.graphics.Color.RED)
                    }
                    icon = defaultIcon
                }
                view.overlays.add(marcador)
            }
            view.invalidate()
        }
    )

    DisposableEffect(Unit) {
        onDispose { mapView.onDetach() }
    }
}
