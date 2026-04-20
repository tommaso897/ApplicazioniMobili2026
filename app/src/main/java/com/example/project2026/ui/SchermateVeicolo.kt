package com.example.project2026.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.data.Veicolo
import com.example.project2026.viewmodel.VeicoloViewModel
import kotlinx.coroutines.launch
import com.example.project2026.data.StatoParcheggio

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVeicoliScreen(
    viewModel: VeicoloViewModel,
    onAggiungiClick: () -> Unit,
    onModificaClick: (Veicolo) -> Unit
) {
    // 1. Osserviamo la lista dei veicoli che arriva dal database tramite il ViewModel
    val listaVeicoli by viewModel.listaVeicoli.collectAsState()

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(title = { Text("I miei Veicoli", color = Color.White) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )

        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAggiungiClick,
                containerColor = Color.Yellow,
                contentColor = Color.Black
            ) {
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
                    SchedaVeicolo(
                        veicolo = veicolo,
                        onDelete = { viewModel.eliminaVeicolo(veicolo) },
                        onModificaClick = onModificaClick
                    )
                }
            }
        }
    }
}

@Composable
fun SchedaVeicolo(veicolo: Veicolo, onDelete: () -> Unit, onModificaClick: (Veicolo) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scegli l'icona in base al tipo di veicolo
                val icona = when (veicolo.tipoVeicolo) {
                    TipoVeicolo.AUTO -> Icons.Default.DirectionsCar
                    TipoVeicolo.MOTO -> Icons.Default.TwoWheeler
                    TipoVeicolo.BICICLETTA -> Icons.Default.DirectionsBike
                    TipoVeicolo.ALTRO -> Icons.Default.DirectionsCar // Icona generica per ALTRO
                }
                Icon(icona, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = veicolo.nome, style = MaterialTheme.typography.titleMedium)
                    Text(text = "Tipo: ${veicolo.tipoVeicolo}", style = MaterialTheme.typography.bodySmall)
                    
                    // Quadrato con stato di parcheggio
                    val (sfondeStato, testoStato) = when (veicolo.statoParcheggio) {
                        StatoParcheggio.LIBERO -> Pair(Color.Black, "LIBERO")
                        StatoParcheggio.PARCHEGGIATO -> Pair(Color(0xFF00AA00), "PARCHEGGIATO") // Verde
                    }
                    
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(sfondeStato)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = testoStato,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }
                }
            }
            // Pulsanti in alto a destra
            Row(
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { onModificaClick(veicolo) },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFDAA520))
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifica veicolo",
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFFF7878))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina veicolo",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VeicoloFormScreen(
    onIndietro: () -> Unit,
    viewModel: VeicoloViewModel = viewModel(),
    veicolo: Veicolo? = null
) {
    var nome by remember { mutableStateOf(veicolo?.nome ?: "") }
    var tipoSelezionato by remember { mutableStateOf(veicolo?.tipoVeicolo ?: TipoVeicolo.AUTO) }
    var expanded by remember { mutableStateOf(false) } // Per il menu a tendina
    val scope = rememberCoroutineScope()
    val isModifica = veicolo != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isModifica) "Modifica Veicolo" else "Nuovo Veicolo") },
                navigationIcon = {
                    IconButton(onClick = onIndietro) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Indietro")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Campo Nome
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Veicolo (es. La mia Panda)") },
                modifier = Modifier.fillMaxWidth()
            )

            // Selettore Tipo Veicolo (Dropdown Menu)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tipoSelezionato.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo di Veicolo") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TipoVeicolo.entries.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo.name) },
                            onClick = {
                                tipoSelezionato = tipo
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Pulsante Salva
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        scope.launch {
                            if (isModifica) {
                                val veicoloAggiornato = veicolo!!.copy(nome = nome, tipoVeicolo = tipoSelezionato)
                                viewModel.modificaVeicolo(veicoloAggiornato)
                            } else {
                                viewModel.aggiungiNuovoVeicolo(nome, tipoSelezionato)
                            }
                            onIndietro() // Torna alla lista dopo il salvataggio
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = nome.isNotBlank()
            ) {
                Text(if (isModifica) "Salva Modifiche" else "Salva Veicolo")
            }
        }
    }
}