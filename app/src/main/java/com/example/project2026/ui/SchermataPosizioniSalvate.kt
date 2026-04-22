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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.project2026.data.PosizioneSalvata
import com.example.project2026.utility.GestorePosizione
import com.example.project2026.viewmodel.PosizioneSalvataViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchermataPosizioniSalvate(
    onIndietro: () -> Unit,
    viewModel: PosizioneSalvataViewModel
) {
    val posizioni by viewModel.tutteLePosizioni.collectAsState()
    var mostraBottomSheet by remember { mutableStateOf(false) }
    var posizioneDaModificare by remember { mutableStateOf<PosizioneSalvata?>(null) }
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val gestorePosizione = remember { GestorePosizione(context) }

    var latRilevata by remember { mutableStateOf<Double?>(null) }
    var lngRilevata by remember { mutableStateOf<Double?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
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
                onClick = {
                    posizioneDaModificare = null
                    val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) {
                        scope.launch {
                            val pos = gestorePosizione.ottieniPosizioneAttuale()
                            latRilevata = pos?.latitude
                            lngRilevata = pos?.longitude
                            mostraBottomSheet = true
                        }
                    } else {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        mostraBottomSheet = true
                    }
                },
                containerColor = Color.Yellow,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Add, contentDescription = "Aggiungi Posizione")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onIndietro) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Indietro", tint = Color.White)
                }
                Text(
                    text = "POSIZIONI SALVATE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (posizioni.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nessuna posizione salvata.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(posizioni) { posizione ->
                        SchedaPosizione(
                            posizione = posizione,
                            onDelete = { viewModel.eliminaPosizione(posizione) },
                            onEdit = {
                                posizioneDaModificare = posizione
                                mostraBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        if (mostraBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { 
                    mostraBottomSheet = false
                    posizioneDaModificare = null
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1C1C1E)
            ) {
                FormGestionePosizione(
                    posizioneEsistente = posizioneDaModificare,
                    latIniziale = latRilevata,
                    lngIniziale = lngRilevata,
                    onSalva = { nome, lat, lng ->
                        if (posizioneDaModificare != null) {
                            // Qui useremo una funzione di update nel futuro se necessario, 
                            // per ora usiamo salvaPosizione che ha REPLACE
                            viewModel.salvaPosizione(nome, lat, lng, id = posizioneDaModificare!!.id)
                        } else {
                            viewModel.salvaPosizione(nome, lat, lng)
                        }
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            mostraBottomSheet = false
                            posizioneDaModificare = null
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SchedaPosizione(posizione: PosizioneSalvata, onDelete: () -> Unit, onEdit: () -> Unit) {
    val context = LocalContext.current
    var indirizzoStr by remember { mutableStateOf("Caricamento indirizzo...") }

    // Rilevamento indirizzo inverso
    LaunchedEffect(posizione.latitudine, posizione.longitudine) {
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(posizione.latitudine, posizione.longitudine, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val via = addr.thoroughfare ?: ""
                    val civico = addr.subThoroughfare ?: ""
                    val citta = addr.locality ?: ""
                    indirizzoStr = if (via.isNotEmpty()) "$via $civico, $citta" else citta
                } else {
                    indirizzoStr = "Indirizzo non trovato"
                }
            } catch (e: Exception) {
                indirizzoStr = "Errore nel caricamento"
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
            // Icona PLACE Verde a sinistra
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF28CD41).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Place, 
                    contentDescription = null, 
                    tint = Color(0xFF28CD41),
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = posizione.nome, 
                    style = MaterialTheme.typography.titleMedium, 
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = indirizzoStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Pulsanti Azione (Modifica e Elimina)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFDAA520))
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Modifica", tint = Color.White, modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFFF7878))
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun FormGestionePosizione(
    posizioneEsistente: PosizioneSalvata? = null,
    latIniziale: Double?,
    lngIniziale: Double?,
    onSalva: (String, Double, Double) -> Unit
) {
    var nome by remember { mutableStateOf(posizioneEsistente?.nome ?: "") }
    var cercaQuery by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var posizioneScelta by remember { 
        mutableStateOf(
            if (posizioneEsistente != null) LatLng(posizioneEsistente.latitudine, posizioneEsistente.longitudine)
            else LatLng(latIniziale ?: 41.9028, lngIniziale ?: 12.4964)
        ) 
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(posizioneScelta, 16f)
    }
    
    val markerState = rememberMarkerState(position = posizioneScelta)

    // Sincronizza se il GPS arriva dopo (solo in modalità creazione)
    LaunchedEffect(latIniziale, lngIniziale) {
        if (posizioneEsistente == null && latIniziale != null && lngIniziale != null) {
            val nuovaPos = LatLng(latIniziale, lngIniziale)
            posizioneScelta = nuovaPos
            markerState.position = nuovaPos
            cameraPositionState.position = CameraPosition.fromLatLngZoom(nuovaPos, 16f)
        }
    }

    fun cercaIndirizzo() {
        if (cercaQuery.isNotBlank()) {
            scope.launch {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val indirizzi = geocoder.getFromLocationName(cercaQuery, 1)
                    if (!indirizzi.isNullOrEmpty()) {
                        val pos = LatLng(indirizzi[0].latitude, indirizzi[0].longitude)
                        posizioneScelta = pos
                        markerState.position = pos
                        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(pos, 16f))
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = if (posizioneEsistente != null) "Modifica Posizione" else "Nuova Posizione",
            style = MaterialTheme.typography.headlineSmall, 
            color = Color.White, 
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome (es. Casa)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = cercaQuery,
            onValueChange = { cercaQuery = it },
            label = { Text("Cerca indirizzo...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            trailingIcon = {
                IconButton(onClick = { cercaIndirizzo() }) {
                    Icon(Icons.Default.Search, contentDescription = "Cerca", tint = Color.Yellow)
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { cercaIndirizzo() })
        )

        Box(
            modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(16.dp)).background(Color.DarkGray)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    posizioneScelta = latLng
                    markerState.position = latLng
                },
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                Marker(state = markerState)
            }
        }

        Button(
            onClick = { if (nome.isNotBlank()) onSalva(nome, posizioneScelta.latitude, posizioneScelta.longitude) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp),
            enabled = nome.isNotBlank()
        ) {
            Text(if (posizioneEsistente != null) "SALVA MODIFICHE" else "SALVA POSIZIONE", fontWeight = FontWeight.Bold)
        }
    }
}
