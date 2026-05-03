package com.example.project2026.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project2026.ParkMateApplication
import com.example.project2026.viewmodel.PosizioneSalvataViewModel
import com.example.project2026.viewmodel.SessioneViewModel
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
    
    val sessioneViewModel: SessioneViewModel = viewModel()
    val posizioneSalvataViewModel: PosizioneSalvataViewModel = viewModel()

    val destinazioniPrincipali = listOf(
        Destinazione.Home,
        Destinazione.ListaVeicoli,
        Destinazione.Cronologia,
        Destinazione.Statistiche
    )
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destinazioneCorrente = navBackStackEntry?.destination?.route

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF3B82F6),
            background = Color.Black,
            surface = Color(0xFF121212),
            onBackground = Color.White,
            onSurface = Color.White
        ),
        typography = Typography(
            bodyLarge = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif
            )
        )
    ) {
        Scaffold(
            topBar = {
                BarraNavigazioneSuperiore(
                    onPosizioniSalvateClick = {
                        navController.navigate(Destinazione.PosizioniSalvate.rotta)
                    }
                )
            },
            bottomBar = {
                BarraNavigazioneInferiore(
                    destinazioneSelezionata = destinazioniPrincipali.find { it.rotta == destinazioneCorrente }
                        ?: Destinazione.Home,
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
                startDestination = Destinazione.Home.rotta,
                modifier = Modifier.padding(padding)
            ) {
                composable(Destinazione.Home.rotta) {
                    SchermataHome(
                        sessioneViewModel = sessioneViewModel,
                        veicoloViewModel = veicoloViewModel,
                        posizioneSalvataViewModel = posizioneSalvataViewModel
                    )
                }

                composable(Destinazione.ListaVeicoli.rotta) {
                    ListaVeicoliScreen(
                        viewModel = veicoloViewModel,
                        sessioneViewModel = sessioneViewModel,
                        posizioneSalvataViewModel = posizioneSalvataViewModel,
                        onAggiungiClick = {
                            navController.navigate(Destinazione.AggiungiVeicolo.rotta)
                        },
                        onModificaClick = { veicolo ->
                            navController.navigate("modifica_veicolo/${veicolo.id}")
                        }
                    )
                }

                composable(Destinazione.AggiungiVeicolo.rotta) {
                    VeicoloFormScreen(
                        onIndietro = { navController.popBackStack() },
                        viewModel = veicoloViewModel
                    )
                }

                composable(
                    route = Destinazione.ModificaVeicolo.rotta,
                    arguments = listOf(navArgument("veicoloId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val veicoloId = backStackEntry.arguments?.getInt("veicoloId")
                    val veicoli by veicoloViewModel.listaVeicoli.collectAsState()
                    val veicolo = veicoli.find { it.id == veicoloId }
                    if (veicolo != null) {
                        VeicoloFormScreen(
                            onIndietro = { navController.popBackStack() },
                            viewModel = veicoloViewModel,
                            veicolo = veicolo
                        )
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Veicolo non trovato")
                        }
                    }
                }

                composable(Destinazione.PosizioniSalvate.rotta) {
                    SchermataPosizioniSalvate(
                        onIndietro = { navController.popBackStack() },
                        viewModel = posizioneSalvataViewModel
                    )
                }

                composable(Destinazione.Cronologia.rotta) {
                    SchermataHistory(
                        sessioneViewModel = sessioneViewModel,
                        veicoloViewModel = veicoloViewModel
                    )
                }
                
                composable(Destinazione.Statistiche.rotta) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Schermata Stats", color = Color.White)
                    }
                }
            }
        }
    }
}
