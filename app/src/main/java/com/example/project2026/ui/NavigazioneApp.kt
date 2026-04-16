package com.example.project2026.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// Importa qui le tue future schermate (che creeremo tra poco)

@Composable
fun NavigazioneApp() {
    // Il NavController è l'oggetto che "guida" effettivamente l'app
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinazione.ListaVeicoli.rotta // Schermata iniziale
    ) {
        // Qui definiamo cosa mostrare per ogni rotta
        composable(Destinazione.ListaVeicoli.rotta) {
            // Qui chiameremo la funzione della schermata lista (es. SchermataListaVeicoli)
            // Per ora mettiamo un segnaposto
            SegnapostoSchermata(titolo = "I miei Veicoli") {
                navController.navigate(Destinazione.AggiungiVeicolo.rotta)
            }
        }

        composable(Destinazione.AggiungiVeicolo.rotta) {
            // Qui chiameremo SchermataAggiungiVeicolo
            SegnapostoSchermata(titolo = "Aggiungi Veicolo") {
                navController.popBackStack() // Torna indietro
            }
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