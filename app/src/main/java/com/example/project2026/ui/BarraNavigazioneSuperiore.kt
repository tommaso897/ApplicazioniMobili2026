package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BarraNavigazioneSuperiore(onPosizioniSalvateClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF10151C))
    ) {
        // Titolo centrato perfettamente nel Box
        Text(
            text = "PARK MATE",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.Center)
        )

        // Icona Bookmarks a destra, centrata verticalmente (Alignment.CenterEnd)
        IconButton(
            onClick = onPosizioniSalvateClick,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Bookmarks,
                contentDescription = "Posizioni Salvate",
                tint = Color(0xFF4087FA),
                modifier = Modifier.size(20.dp) // Dimensione ridotta a 20.dp per maggior eleganza
            )
        }
    }
}
