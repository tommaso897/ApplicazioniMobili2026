package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BarraNavigazioneInferiore(
    destinazioneSelezionata: Destinazione,
    onDestinazioneClick: (Destinazione) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0D1117))
    ) {
        // Linea blu sottile superiore
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFF1E3A5F))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BarraItem(
                icona = Icons.Default.Home,
                testo = "Home",
                selezionato = destinazioneSelezionata is Destinazione.Home,
                onClick = { onDestinazioneClick(Destinazione.Home) }
            )
            BarraItem(
                icona = Icons.Default.DirectionsCar,
                testo = "Garage",
                selezionato = destinazioneSelezionata is Destinazione.ListaVeicoli,
                onClick = { onDestinazioneClick(Destinazione.ListaVeicoli) }
            )
            BarraItem(
                icona = Icons.Default.History,
                testo = "History",
                selezionato = destinazioneSelezionata is Destinazione.Cronologia,
                onClick = { onDestinazioneClick(Destinazione.Cronologia) }
            )
            BarraItem(
                icona = Icons.Default.BarChart,
                testo = "Stats",
                selezionato = destinazioneSelezionata is Destinazione.Statistiche,
                onClick = { onDestinazioneClick(Destinazione.Statistiche) }
            )
        }
    }
}

@Composable
private fun BarraItem(
    icona: androidx.compose.ui.graphics.vector.ImageVector,
    testo: String,
    selezionato: Boolean,
    onClick: () -> Unit
) {
    val coloreBlue = Color(0xFF3B82F6)
    val coloreGray = Color(0xFF6B7280)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Indicatore pill blu sopra l'icona selezionata
        if (selezionato) {
            Box(
                modifier = Modifier
                    .width(24.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(coloreBlue)
            )
            Spacer(modifier = Modifier.size(4.dp))
        } else {
            Spacer(modifier = Modifier.size(6.dp))
        }

        Icon(
            icona,
            contentDescription = testo,
            tint = if (selezionato) coloreBlue else coloreGray,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(2.dp))
        Text(
            testo,
            color = if (selezionato) coloreBlue else coloreGray,
            style = MaterialTheme.typography.labelSmall
        )
    }
}
