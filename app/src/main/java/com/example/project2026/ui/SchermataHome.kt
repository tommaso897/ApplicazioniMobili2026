package com.example.project2026.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.utility.GestorePosizione
import com.example.project2026.viewmodel.PosizioneSalvataViewModel
import com.example.project2026.viewmodel.SessioneViewModel
import com.example.project2026.viewmodel.VeicoloViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun SchermataHome(
    sessioneViewModel: SessioneViewModel,
    veicoloViewModel: VeicoloViewModel,
    posizioneSalvataViewModel: PosizioneSalvataViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gestorePosizione = remember { GestorePosizione(context) }
    
    val sessioniAttive by sessioneViewModel.sessioniAttive.collectAsState()
    val veicoli by veicoloViewModel.listaVeicoli.collectAsState()
    val posizioniSalvate by posizioneSalvataViewModel.tutteLePosizioni.collectAsState()
    
    var cercaQuery by remember { mutableStateOf("") }
    var permessiConcessi by remember { 
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    var mostraPopUpTermina by remember { mutableStateOf(false) }
    var sessioneSelezionata by remember { mutableStateOf<SessioneParcheggio?>(null) }
    var costoCalcolatoFinale by remember { mutableStateOf(0.0) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(41.9028, 12.4964), 12f)
    }

    fun cercaIndirizzo() {
        if (cercaQuery.isNotBlank()) {
            scope.launch {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val indirizzi = geocoder.getFromLocationName(cercaQuery, 1)
                    if (!indirizzi.isNullOrEmpty()) {
                        val pos = LatLng(indirizzi[0].latitude, indirizzi[0].longitude)
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 15f))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permessiConcessi = permissions.values.all { it }
        if (permessiConcessi) {
            scope.launch {
                val pos = gestorePosizione.ottieniPosizioneAttuale()
                pos?.let {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!permessiConcessi) {
            permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        } else {
            val pos = gestorePosizione.ottieniPosizioneAttuale()
            pos?.let {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(0.45f)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permessiConcessi),
                uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = true)
            ) {
                posizioniSalvate.forEach { pos ->
                    Marker(
                        state = MarkerState(position = LatLng(pos.latitudine, pos.longitudine)),
                        title = pos.nome,
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
                    )
                }

                sessioniAttive.forEach { sessione ->
                    val veicolo = veicoli.find { it.id == sessione.idVeicolo }
                    if (sessione.latitudine != null && sessione.longitudine != null) {
                        Marker(
                            state = MarkerState(position = LatLng(sessione.latitudine, sessione.longitudine)),
                            title = veicolo?.nome ?: "Veicolo in sosta",
                            snippet = "Sosta ${sessione.tipo.name}",
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = cercaQuery,
                onValueChange = { cercaQuery = it },
                placeholder = { Text("Cerca luogo...", color = Color.Gray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1C1C1E).copy(alpha = 0.9f),
                    unfocusedContainerColor = Color(0xFF1C1C1E).copy(alpha = 0.8f),
                    focusedBorderColor = Color.Yellow,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    IconButton(onClick = { cercaIndirizzo() }) {
                        Icon(Icons.Default.Search, contentDescription = "Cerca", tint = Color.Yellow)
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { cercaIndirizzo() }),
                singleLine = true
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.55f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "SOSTE ATTIVE",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (sessioniAttive.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessun veicolo in sosta.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(sessioniAttive) { sessione ->
                        val veicolo = veicoli.find { it.id == sessione.idVeicolo }
                        if (veicolo != null) {
                            SchedaSostaAttiva(
                                sessione = sessione,
                                nomeVeicolo = veicolo.nome,
                                tipoVeicolo = veicolo.tipoVeicolo,
                                onTerminaClick = { costo ->
                                    costoCalcolatoFinale = costo
                                    sessioneSelezionata = sessione
                                    mostraPopUpTermina = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostraPopUpTermina && sessioneSelezionata != null) {
        PopUpFineSosta(
            costo = costoCalcolatoFinale,
            onConferma = { salvaPos, nomePos ->
                if (salvaPos && nomePos.isNotBlank()) {
                    posizioneSalvataViewModel.salvaPosizione(
                        nome = nomePos,
                        lat = sessioneSelezionata!!.latitudine ?: 0.0,
                        lng = sessioneSelezionata!!.longitudine ?: 0.0
                    )
                }
                sessioneViewModel.terminaParcheggio(sessioneSelezionata!!)
                mostraPopUpTermina = false
                sessioneSelezionata = null
            },
            onAnnulla = {
                mostraPopUpTermina = false
                sessioneSelezionata = null
            }
        )
    }
}

@Composable
fun PopUpFineSosta(
    costo: Double,
    onConferma: (Boolean, String) -> Unit,
    onAnnulla: () -> Unit
) {
    var salvaPosizione by remember { mutableStateOf(false) }
    var nomePosizione by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onAnnulla,
        containerColor = Color(0xFF1C1C1E),
        title = {
            Text("Fine Sosta", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Costo totale: ${String.format("%.2f", costo)}€",
                    color = Color.Yellow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Vuoi salvare questa posizione?", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(
                        checked = salvaPosizione,
                        onCheckedChange = { salvaPosizione = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Yellow)
                    )
                }

                if (salvaPosizione) {
                    OutlinedTextField(
                        value = nomePosizione,
                        onValueChange = { nomePosizione = it },
                        label = { Text("Nome posizione (es. Lavoro)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConferma(salvaPosizione, nomePosizione) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A))
            ) {
                Text("TERMINA", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onAnnulla) {
                Text("ANNULLA", color = Color.Gray)
            }
        }
    )
}

@Composable
fun SchedaSostaAttiva(
    sessione: SessioneParcheggio,
    nomeVeicolo: String,
    tipoVeicolo: TipoVeicolo,
    onTerminaClick: (Double) -> Unit
) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val diff = if (sessione.tipo == TipoParcheggio.TICKET) (sessione.scadenza ?: 0L) - currentTime else currentTime - sessione.inizio
    val costoAttuale = when (sessione.tipo) {
        TipoParcheggio.PAID -> if (sessione.tariffa != null) ( (currentTime - sessione.inizio) / (1000.0 * 60 * 60)) * sessione.tariffa else 0.0
        TipoParcheggio.TICKET -> sessione.costo ?: 0.0
        else -> 0.0
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icona = when (tipoVeicolo) {
                    TipoVeicolo.AUTO -> Icons.Default.DirectionsCar
                    TipoVeicolo.MOTO -> Icons.Default.TwoWheeler
                    TipoVeicolo.BICICLETTA -> Icons.Default.DirectionsBike
                    else -> Icons.Default.DirectionsCar
                }
                Box(
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icona, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = nomeVeicolo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Sosta ${sessione.tipo.name}", color = Color.Gray, fontSize = 11.sp)
                }
            }

            // Visualizzazione NOTE se presenti
            if (!sessione.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notes, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = sessione.note,
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(text = "PARCHEGGIATO", color = Color(0xFF28CD41), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    when (sessione.tipo) {
                        TipoParcheggio.PAID -> {
                            Text("Tempo: ${formattaMillis(diff)}", color = Color.White, fontSize = 13.sp)
                            Text("Costo: ${String.format("%.2f", costoAttuale)}€", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        TipoParcheggio.TICKET -> {
                            val countdownStr = if (diff > 0) formattaMillis(diff) else "SCADUTO"
                            Text("Mancano: $countdownStr", color = if (diff > 0) Color.Cyan else Color.Red, fontSize = 13.sp)
                            Text("Costo: ${String.format("%.2f", costoAttuale)}€", color = Color.White, fontSize = 11.sp)
                        }
                        else -> {
                            Text("Inizio: ${sessione.dataInizio}", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Button(
                    onClick = { onTerminaClick(costoAttuale) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("TERMINA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

fun formattaMillis(millis: Long): String {
    val m = if (millis < 0) 0L else millis
    val ore = TimeUnit.MILLISECONDS.toHours(m)
    val minuti = TimeUnit.MILLISECONDS.toMinutes(m) % 60
    val secondi = TimeUnit.MILLISECONDS.toSeconds(m) % 60
    return String.format("%02d:%02d:%02d", ore, minuti, secondi)
}
