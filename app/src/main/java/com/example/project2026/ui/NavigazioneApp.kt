package com.example.project2026.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project2026.ParkMateApplication
import com.example.project2026.viewmodel.VeicoloViewModel
import com.example.project2026.viewmodel.VeicoloViewModelFactory

@Composable
fun NavigazioneApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val app = context.applicationContext as ParkMateApplication
    val veicoloViewModel: VeicoloViewModel = viewModel(
        factory = VeicoloViewModelFactory(app.repository)
    )
    // Stato locale per la destinazione selezionata
    val destinazioniPrincipali = listOf(
        Destinazione.Mappa,
        Destinazione.ListaVeicoli,
        Destinazione.Cronologia,
        Destinazione.Statistiche
    )
    val iconePrincipali = listOf(
        Icons.Default.Map,
        Icons.Default.DirectionsCar,
        Icons.Default.History,
        Icons.Default.BarChart
    )
    val etichettePrincipali = listOf(
        "Mappa", "Garage", "History", "Stats"
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    // Ricava la destinazione attuale
    val destinazioneCorrente = navBackStackEntry?.destination?.route
    // AGGIUNTA: Definizione del tema globale
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF3B82F6),   // Blu per le selezioni
            background = Color.Black,      // Sfondo nero globale
            surface = Color(0xFF121212),   // Superficie delle card
            onBackground = Color.White,    // Testo bianco
            onSurface = Color.White        // Testo su card bianco
        ),
        typography = Typography(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif // O un font arrotondato se disponibile
            )
        )
    ) {
        // Qui dentro ci deve essere il tuo Scaffold con il NavHost

        // Scaffold con bottom bar
        Scaffold(
            bottomBar = {
                BarraNavigazioneInferiore(
                    destinazioneSelezionata = destinazioniPrincipali.find { it.rotta == destinazioneCorrente }
                        ?: Destinazione.ListaVeicoli,
                    onDestinazioneClick = { destinazione ->
                        navController.navigate(destinazione.rotta) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Destinazione.ListaVeicoli.rotta,
                modifier = Modifier.padding(padding)
            ) {
                // Qui definiamo cosa mostrare per ogni rotta
                composable(Destinazione.ListaVeicoli.rotta) {
                    // Qui chiameremo la schermata che mostra la lista dei veicoli
                    ListaVeicoliScreen(
                        viewModel = veicoloViewModel,
                        onAggiungiClick = {
                            navController.navigate(Destinazione.AggiungiVeicolo.rotta)
                        }
                    )
                }

                composable(Destinazione.AggiungiVeicolo.rotta) {
                    // Qui passeremo alla prossima fase: il form di inserimento
                    AggiungiVeicoloScreen(
                        onIndietro = { navController.popBackStack() },
                        viewModel = veicoloViewModel
                    )
                }

                composable(Destinazione.Mappa.rotta) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Schermata Mappa")
                    }
                }
                composable(Destinazione.Cronologia.rotta) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Schermata History")
                    }
                }
                composable(Destinazione.Statistiche.rotta) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Schermata Stats")
                    }
                }
            }
        }
    }
}
    // Funzione temporanea solo per testare se la navigazione funziona
    @Composable
    fun SegnapostoSchermata(titolo: String, onAzione: () -> Unit) {
        // In futuro qui ci sarà il codice vero delle tue pagine
    }
