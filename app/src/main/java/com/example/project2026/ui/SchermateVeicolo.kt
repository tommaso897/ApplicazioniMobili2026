package com.example.project2026.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.project2026.data.Veicolo
import com.example.project2026.viewmodel.VeicoloViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVeicoliScreen(
    viewModel: VeicoloViewModel,
    onAggiungiClick: () -> Unit
) {
    // 1. Osserviamo la lista dei veicoli che arriva dal database tramite il ViewModel
    val listaVeicoli by viewModel.listaVeicoli.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("I miei Veicoli") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAggiungiClick) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Veicolo")
            }
        }
    ) { padding ->
        // 2. Gestiamo il caso in cui il database sia vuoto
        if (listaVeicoli.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nessun veicolo salvato. Premi + per iniziare!")
            }
        } else {
            // 3. Mostriamo la lista scorrevole dei veicoli
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(listaVeicoli) { veicolo ->
                    SchedaVeicolo(veicolo)
                }
            }
        }
    }
}

@Composable
fun SchedaVeicolo(veicolo: Veicolo) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.DirectionsCar, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = veicolo.nome, style = MaterialTheme.typography.titleMedium)
                Text(text = "Tipo: ${veicolo.tipoVeicolo}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// Segnaposto per la schermata di aggiunta (la faremo al passo 4)
@Composable
fun AggiungiVeicoloScreen(onIndietro: () -> Unit) {
    Text(text = "Schermata di inserimento in arrivo...")
}