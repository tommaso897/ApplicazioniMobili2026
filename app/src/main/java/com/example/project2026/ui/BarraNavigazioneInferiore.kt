package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BarraNavigazioneInferiore(
    destinazioneSelezionata: Destinazione,
    onDestinazioneClick: (Destinazione) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10151C))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home
        BarraItem(
            icona = Icons.Default.Home,
            testo = "Home",
            selezionato = destinazioneSelezionata is Destinazione.Home,
            onClick = { onDestinazioneClick(Destinazione.Home) }
        )
        // Garage
        BarraItem(
            icona = Icons.Default.DirectionsCar,
            testo = "Garage",
            selezionato = destinazioneSelezionata is Destinazione.ListaVeicoli,
            onClick = { onDestinazioneClick(Destinazione.ListaVeicoli) }
        )
        // History
        BarraItem(
            icona = Icons.Default.History,
            testo = "History",
            selezionato = destinazioneSelezionata is Destinazione.Cronologia,
            onClick = { onDestinazioneClick(Destinazione.Cronologia) }
        )
        // Stats
        BarraItem(
            icona = Icons.Default.BarChart,
            testo = "Stats",
            selezionato = destinazioneSelezionata is Destinazione.Statistiche,
            onClick = { onDestinazioneClick(Destinazione.Statistiche) }
        )
    }
}

@Composable
private fun BarraItem(
    icona: androidx.compose.ui.graphics.vector.ImageVector,
    testo: String,
    selezionato: Boolean,
    onClick: () -> Unit
) {
    val colore = if (selezionato) Color(0xFF4087FA) else Color(0xFFB0B8C1)
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Icon(icona, contentDescription = testo, tint = colore, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.size(2.dp))
        Text(testo, color = colore, style = MaterialTheme.typography.labelSmall)
    }
}
