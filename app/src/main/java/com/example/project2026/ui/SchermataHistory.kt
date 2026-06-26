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

    var filtroSelezionato by remember { mutableStateOf("TUTTI") }
    val opzioniFiltro = listOf("TUTTI", "LIBERO", "ORARIO", "TICKET")

    val cronologiaFiltrata = remember(cronologia, filtroSelezionato) {
        if (filtroSelezionato == "TUTTI") cronologia
        else cronologia.filter { sessione ->
            when (filtroSelezionato) {
                "LIBERO" -> sessione.tipo == TipoParcheggio.FREE
                "ORARIO" -> sessione.tipo == TipoParcheggio.PAID
                "TICKET" -> sessione.tipo == TipoParcheggio.TICKET
                else -> true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E17))
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        // Header
        Text(
            text = "CRONOLOGIA SOSTE",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFF9FAFB),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Filtri
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            opzioniFiltro.forEach { filtro ->
                val selezionato = filtroSelezionato == filtro
                FilterChip(
                    selected = selezionato,
                    onClick = { filtroSelezionato = filtro },
                    label = { Text(filtro, fontSize = 12.sp, fontWeight = if (selezionato) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF111827),
                        labelColor = Color(0xFF9CA3AF),
                        selectedContainerColor = Color(0xFF1E3A5F),
                        selectedLabelColor = Color(0xFF3B82F6)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selezionato,
                        borderColor = Color(0xFF2D3748),
                        selectedBorderColor = Color(0xFF3B82F6)
                    )
                )
            }
        }

        if (cronologiaFiltrata.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF374151), modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filtroSelezionato == "TUTTI") "Nessuna sosta archiviata."
                               else "Nessuna sosta di tipo $filtroSelezionato.",
                        color = Color(0xFF6B7280),
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

    // Colore e label del tipo parcheggio
    val (coloreTipo, labelTipo) = when (sessione.tipo) {
        TipoParcheggio.FREE -> Color(0xFF10B981) to "LIBERO"
        TipoParcheggio.PAID -> Color(0xFFF59E0B) to "ORARIO"
        TipoParcheggio.TICKET -> Color(0xFF3B82F6) to "TICKET"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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

            // Icona veicolo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1F2937)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icona, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(26.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = nomeVeicolo,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFF9FAFB),
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Badge tipo
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(coloreTipo.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = labelTipo,
                        color = coloreTipo,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = indirizzoStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "▶ ${sessione.dataInizio}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                    if (sessione.dataFine != null) {
                        Text(text = "■ ${sessione.dataFine}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF9CA3AF))
                    }
                }
            }

            if (sessione.costo != null && sessione.costo!! > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format("%.2f€", sessione.costo),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color(0xFFF9FAFB),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(text = "TOTALE", style = MaterialTheme.typography.labelSmall, color = Color(0xFF6B7280))
                }
            }
        }
    }
}
