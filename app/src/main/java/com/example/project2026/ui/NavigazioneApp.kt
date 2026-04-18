package com.example.project2026.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.project2026.ParkMateApplication
import com.example.project2026.viewmodel.VeicoloViewModel
import com.example.project2026.viewmodel.VeicoloViewModelFactory

// Importa qui le tue future schermate (che creeremo tra poco)

@Composable
fun NavigazioneApp() {
    // Il NavController è l'oggetto che "guida" effettivamente l'app
    val navController = rememberNavController()

    // Recuperiamo l'istanza dell'applicazione e il repository
    val context = LocalContext.current
    val app = context.applicationContext as ParkMateApplication

    val veicoloViewModel: VeicoloViewModel = viewModel(
        factory = VeicoloViewModelFactory(app.repository)
    )

    NavHost(
        navController = navController,
        startDestination = Destinazione.ListaVeicoli.rotta // Schermata iniziale
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
            AggiungiVeicoloScreen(onIndietro = { navController.popBackStack() })
        }

        composable(Destinazione.Mappa.rotta) {
            // Futura schermata mappa
        }
    }
}

// Funzione temporanea solo per testare se la navigazione funziona
@Composable
fun SegnapostoSchermata(titolo: String, onAzione: () -> Unit) {
    // In futuro qui ci sarà il codice vero delle tue pagine
}