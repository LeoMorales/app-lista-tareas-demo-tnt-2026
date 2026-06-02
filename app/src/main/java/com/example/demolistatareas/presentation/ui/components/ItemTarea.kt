package com.example.demolistatareas.presentation.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.demolistatareas.domain.model.Tarea

@Composable
fun ItemTarea(
    tarea: Tarea,
    onAlternarEstado: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = tarea.estaCompletada,
            onCheckedChange = { onAlternarEstado(tarea.id) }
        )

        // El texto ocupa todo el espacio disponible empujando los íconos al final
        Text(
            text = tarea.titulo,
            modifier = Modifier.weight(1f),
            textDecoration = if (tarea.estaCompletada) TextDecoration.LineThrough else null
        )

        // mostrar distinción si hay ubicación
        if (tarea.ubicacion != null) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = "Tiene ubicación asignada",
                // Usamos un color secundario y un tamaño menor para que no compita con el texto
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp).padding(end = 8.dp)
            )
        }
    }
}