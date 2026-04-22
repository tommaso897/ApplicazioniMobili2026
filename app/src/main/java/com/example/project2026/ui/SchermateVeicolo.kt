package com.example.project2026.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.project2026.data.PosizioneSalvata
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.TipoVeicolo
import com.example.project2026.data.Veicolo
import com.example.project2026.utility.GestorePosizione
import com.example.project2026.viewmodel.PosizioneSalvataViewModel
import com.example.project2026.viewmodel.SessioneViewModel
import com.example.project2026.viewmodel.VeicoloViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaVeicoliScreen(
    viewModel: VeicoloViewModel,
    sessioneViewModel: SessioneViewModel,
    posizioneSalvataViewModel: PosizioneSalvataViewModel,
    onAggiungiClick: () -> Unit,
    onModificaClick: (Veicolo) -> Unit
) {
    val listaVeicoli by viewModel.listaVeicoli.collectAsState()
    val sessioniAttive by sessioneViewModel.sessioniAttive.collectAsState()
    val posizioniSalvate by posizioneSalvataViewModel.tutteLePosizioni.collectAsState()
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gestorePosizione = remember { GestorePosizione(context) }
    
    var veicoloSelezionato by remember { mutableStateOf<Veicolo?>(null) }
    var mostraBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var latRilevata by remember { mutableStateOf<Double?>(null) }
    var lngRilevata by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            scope.launch {
                val pos = gestorePosizione.ottieniPosizioneAttuale()
                latRilevata = pos?.latitude
                lngRilevata = pos?.longitude
            }
        }
    }

    Scaffold(
        containerColor = Color.Black,
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
        if (listaVeicoli.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Nessun veicolo salvato. Premi + per iniziare!", color = Color.White)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(listaVeicoli) { veicolo ->
                    val sessioneAttiva = sessioniAttive.find { it.idVeicolo == veicolo.id }
                    
                    SchedaVeicolo(
                        veicolo = veicolo,
                        onDelete = { viewModel.eliminaVeicolo(veicolo) },
                        onModificaClick = onModificaClick,
                        onIniziaParcheggio = {
                            if (veicolo.statoParcheggio == StatoParcheggio.LIBERO) {
                                veicoloSelezionato = it
                                mostraBottomSheet = true
                                
                                latRilevata = null
                                lngRilevata = null

                                val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFineLocation) {
                                    scope.launch {
                                        val pos = gestorePosizione.ottieniPosizioneAttuale()
                                        latRilevata = pos?.latitude
                                        lngRilevata = pos?.longitude
                                    }
                                } else {
                                    permissionLauncher.launch(arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    ))
                                }
                            } else {
                                sessioneAttiva?.let { s -> sessioneViewModel.terminaParcheggio(s) }
                            }
                        }
                    )
                }
            }
        }

        if (mostraBottomSheet && veicoloSelezionato != null) {
            ModalBottomSheet(
                onDismissRequest = { 
                    mostraBottomSheet = false
                    veicoloSelezionato = null
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1E)
            ) {
                SchermataGestioneParcheggio(
                    veicolo = veicoloSelezionato!!,
                    latIniziale = latRilevata,
                    lngIniziale = lngRilevata,
                    posizioniSalvate = posizioniSalvate,
                    onConferma = { tipo, tariffa, scadenza, costo, lat, lng ->
                        sessioneViewModel.iniziaParcheggio(
                            veicolo = veicoloSelezionato!!,
                            tipo = tipo,
                            tariffa = tariffa,
                            scadenza = scadenza,
                            costoIniziale = costo,
                            lat = lat,
                            lng = lng
                        )
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            mostraBottomSheet = false
                            veicoloSelezionato = null
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataGestioneParcheggio(
    veicolo: Veicolo,
    latIniziale: Double?,
    lngIniziale: Double?,
    posizioniSalvate: List<PosizioneSalvata>,
    onConferma: (TipoParcheggio, Double?, Long?, Double?, Double?, Double?) -> Unit
) {
    var tipoSelezionato by remember { mutableStateOf(TipoParcheggio.FREE) }
    var tariffaStr by remember { mutableStateOf("") }
    var costoStr by remember { mutableStateOf("") }
    var minutiScadenzaStr by remember { mutableStateOf("") }
    
    var posizioneScelta by remember { 
        mutableStateOf(LatLng(latIniziale ?: 41.9028, lngIniziale ?: 12.4964)) 
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posizioneScelta, 15f)
    }

    val markerState = rememberMarkerState(position = posizioneScelta)

    var mappaPronta by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(600)
        mappaPronta = true
    }

    LaunchedEffect(latIniziale, lngIniziale) {
        if (latIniziale != null && lngIniziale != null) {
            val nuovaPos = LatLng(latIniziale, lngIniziale)
            posizioneScelta = nuovaPos
            markerState.position = nuovaPos
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(nuovaPos, 15f))
        }
    }

    LaunchedEffect(posizioneScelta) {
        markerState.position = posizioneScelta
    }

    var expanded by remember { mutableStateOf(false) }
    var posizioneSelezionataNome by remember { mutableStateOf("Seleziona posizione salvata") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Parcheggio: ${veicolo.nome}",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OpzioneParcheggio(
                titolo = "Libero",
                icona = Icons.Default.LocationOn,
                colore = if (tipoSelezionato == TipoParcheggio.FREE) Color.Green else Color.Gray,
                onClick = { tipoSelezionato = TipoParcheggio.FREE }
            )
            OpzioneParcheggio(
                titolo = "Orario",
                icona = Icons.Default.AccessTime,
                colore = if (tipoSelezionato == TipoParcheggio.PAID) Color.Yellow else Color.Gray,
                onClick = { tipoSelezionato = TipoParcheggio.PAID }
            )
            OpzioneParcheggio(
                titolo = "Ticket",
                icona = Icons.Default.ConfirmationNumber,
                colore = if (tipoSelezionato == TipoParcheggio.TICKET) Color.Cyan else Color.Gray,
                onClick = { tipoSelezionato = TipoParcheggio.TICKET }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            if (mappaPronta) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        posizioneScelta = latLng
                        posizioneSelezionataNome = "Posizione personalizzata"
                    },
                    properties = remember(latIniziale) { 
                        MapProperties(isMyLocationEnabled = latIniziale != null) 
                    },
                    uiSettings = remember { 
                        MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false) 
                    }
                ) {
                    Marker(
                        state = markerState,
                        title = "Punto di Sosta"
                    )
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.Yellow, modifier = Modifier.size(30.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Caricamento mappa...", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = posizioneSelezionataNome,
                onValueChange = {},
                readOnly = true,
                label = { Text("Posizioni Salvate") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                    unfocusedTextColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedBorderColor = Color.Yellow
                ),
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color(0xFF2C2C2E))
            ) {
                DropdownMenuItem(
                    text = { Text("Nessuna (usa mappa)", color = Color.White) },
                    onClick = {
                        posizioneSelezionataNome = "Nessuna (usa mappa)"
                        expanded = false
                    }
                )
                posizioniSalvate.forEach { pos ->
                    DropdownMenuItem(
                        text = { Text(pos.nome, color = Color.White) },
                        onClick = {
                            posizioneSelezionataNome = pos.nome
                            posizioneScelta = LatLng(pos.latitudine, pos.longitudine)
                            markerState.position = posizioneScelta
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (tipoSelezionato) {
            TipoParcheggio.PAID -> {
                OutlinedTextField(
                    value = tariffaStr,
                    onValueChange = { tariffaStr = it },
                    label = { Text("Tariffa Oraria (€/h)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
            TipoParcheggio.TICKET -> {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = costoStr,
                        onValueChange = { costoStr = it },
                        label = { Text("Costo Fisso (€)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = minutiScadenzaStr,
                        onValueChange = { minutiScadenzaStr = it },
                        label = { Text("Durata (minuti)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val tariffa = tariffaStr.toDoubleOrNull()
                val costo = costoStr.toDoubleOrNull()
                val scadenzaMs = minutiScadenzaStr.toLongOrNull()?.let { System.currentTimeMillis() + (it * 60 * 1000) }
                
                onConferma(
                    tipoSelezionato, 
                    tariffa, 
                    scadenzaMs, 
                    costo, 
                    posizioneScelta.latitude, 
                    posizioneScelta.longitude
                )
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFERMA SOSTA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SchedaVeicolo(
    veicolo: Veicolo,
    onDelete: () -> Unit,
    onModificaClick: (Veicolo) -> Unit,
    onIniziaParcheggio: (Veicolo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable { onIniziaParcheggio(veicolo) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val icona = when (veicolo.tipoVeicolo) {
                    TipoVeicolo.AUTO -> Icons.Default.DirectionsCar
                    TipoVeicolo.MOTO -> Icons.Default.TwoWheeler
                    TipoVeicolo.BICICLETTA -> Icons.Default.DirectionsBike
                    TipoVeicolo.ALTRO -> Icons.Default.DirectionsCar
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2C2C2E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icona, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = veicolo.nome,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = veicolo.tipoVeicolo.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    val (sfondoStato, testoStato) = when (veicolo.statoParcheggio) {
                        StatoParcheggio.LIBERO -> Pair(Color(0xFF3A3A3C), "LIBERO")
                        StatoParcheggio.PARCHEGGIATO -> Pair(Color(0xFF28CD41).copy(alpha = 0.2f), "IN SOSTA")
                    }
                    val coloreTestoStato = if (veicolo.statoParcheggio == StatoParcheggio.PARCHEGGIATO) Color(0xFF28CD41) else Color.White

                    Box(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(sfondoStato)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = testoStato,
                            style = MaterialTheme.typography.labelSmall,
                            color = coloreTestoStato
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { onModificaClick(veicolo) },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDAA520))
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifica",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF7878))
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OpzioneParcheggio(titolo: String, icona: ImageVector, colore: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(8.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(colore.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icona, contentDescription = titolo, tint = colore, modifier = Modifier.size(32.dp))
        }
        Text(titolo, style = MaterialTheme.typography.bodyMedium, color = Color.White, modifier = Modifier.padding(top = 8.dp))
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
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val isModifica = veicolo != null

    Scaffold(
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onIndietro) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Color.White)
            }

            Text(
                text = if (isModifica) "Modifica Veicolo" else "Nuovo Veicolo",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome Veicolo (es. La mia Panda)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

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
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.fillMaxWidth()) {
                    TipoVeicolo.entries.forEach { tipo ->
                        DropdownMenuItem(text = { Text(tipo.name) }, onClick = { tipoSelezionato = tipo; expanded = false })
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

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
                            onIndietro()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = nome.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isModifica) "Salva Modifiche" else "Salva Veicolo", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
