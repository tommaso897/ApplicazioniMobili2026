package com.example.project2026.ui

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.viewmodel.SessioneViewModel
import com.example.project2026.viewmodel.VeicoloViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataHistory(
    sessioneViewModel: SessioneViewModel,
    veicoloViewModel: VeicoloViewModel
) {
    val cronologia by sessioneViewModel.cronologiaTerminate.collectAsState()
    val veicoli by veicoloViewModel.listaVeicoli.collectAsState()

    // Stato per il filtro selezionato
    var filtroSelezionato by remember { mutableStateOf("TUTTI") }
    val opzioniFiltro = listOf("TUTTI", "LIBERO", "ORARIO", "TICKET")

    // Applichiamo il filtro alla lista
    val cronologiaFiltrata = remember(cronologia, filtroSelezionato) {
        if (filtroSelezionato == "TUTTI") {
            cronologia
        } else {
            cronologia.filter { sessione ->
                when (filtroSelezionato) {
                    "LIBERO" -> sessione.tipo == TipoParcheggio.FREE
                    "ORARIO" -> sessione.tipo == TipoParcheggio.PAID
                    "TICKET" -> sessione.tipo == TipoParcheggio.TICKET
                    else -> true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "CRONOLOGIA SOSTE",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Barra dei FILTRI
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opzioniFiltro.forEach { filtro ->
                val selezionato = filtroSelezionato == filtro
                FilterChip(
                    selected = selezionato,
                    onClick = { filtroSelezionato = filtro },
                    label = { Text(filtro, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF1C1C1E),
                        labelColor = Color.Gray,
                        selectedContainerColor = Color.Yellow,
                        selectedLabelColor = Color.Black
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selezionato,
                        borderColor = Color.DarkGray,
                        selectedBorderColor = Color.Yellow
                    )
                )
            }
        }

        if (cronologiaFiltrata.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (filtroSelezionato == "TUTTI") "Nessuna sosta archiviata." 
                          else "Nessuna sosta di tipo $filtroSelezionato.", 
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(cronologiaFiltrata) { sessione ->
                    val veicolo = veicoli.find { it.id == sessione.idVeicolo }
                    SchedaHistory(
                        sessione = sessione,
                        nomeVeicolo = veicolo?.nome ?: "Veicolo sconosciuto",
                        tipoVeicolo = veicolo?.tipoVeicolo ?: TipoVeicolo.AUTO
                    )
                }
            }
        }
    }
}

@Composable
fun SchedaHistory(
    sessione: SessioneParcheggio,
    nomeVeicolo: String,
    tipoVeicolo: TipoVeicolo
) {
    val context = LocalContext.current
    var indirizzoStr by remember { mutableStateOf("Indirizzo non disponibile") }

    LaunchedEffect(sessione.latitudine, sessione.longitudine) {
        if (sessione.latitudine != null && sessione.longitudine != null) {
            withContext(Dispatchers.IO) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(sessione.latitudine, sessione.longitudine, 1)
                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        val via = addr.thoroughfare ?: ""
                        val civico = addr.subThoroughfare ?: ""
                        val citta = addr.locality ?: ""
                        indirizzoStr = if (via.isNotEmpty()) "$via $civico, $citta" else citta
                    }
                } catch (e: Exception) {
                    indirizzoStr = "Errore caricamento indirizzo"
                }
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icona = when (tipoVeicolo) {
                TipoVeicolo.AUTO -> Icons.Default.DirectionsCar
                TipoVeicolo.MOTO -> Icons.Default.TwoWheeler
                TipoVeicolo.BICICLETTA -> Icons.Default.DirectionsBike
                else -> Icons.Default.DirectionsCar
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C2C2E)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icona, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nomeVeicolo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                val etichettaTipo = when(sessione.tipo) {
                    TipoParcheggio.FREE -> "LIBERO"
                    TipoParcheggio.PAID -> "ORARIO"
                    TipoParcheggio.TICKET -> "TICKET"
                }
                
                Text(
                    text = "Tipo: $etichettaTipo",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Yellow,
                    fontSize = 11.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = indirizzoStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column {
                    Text(
                        text = "Inizio: ${sessione.dataInizio}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                    Text(
                        text = "Fine:   ${sessione.dataFine ?: "-"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }

            if (sessione.costo != null && sessione.costo!! > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${String.format("%.2f", sessione.costo)}€",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "TOTALE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}
