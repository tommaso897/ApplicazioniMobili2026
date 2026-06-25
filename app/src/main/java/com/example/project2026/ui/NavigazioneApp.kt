package com.example.project2026.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontFamily
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.project2026.viewmodel.PosizioneSalvataViewModel
import com.example.project2026.viewmodel.SessioneViewModel
import com.example.project2026.viewmodel.UtenteViewModel
import com.example.project2026.viewmodel.VeicoloViewModel

@Composable
fun NavigazioneApp() {
    val navController = rememberNavController()

    // ViewModel autenticazione — disponibile ovunque nell'app
    val utenteViewModel: UtenteViewModel = viewModel()

    // Tutti i ViewModel usano SessionManager.utenteCorrente (flow reattivo):
    // non serve più passare idUtente come parametro né usare factory personalizzate.
    val veicoloViewModel: VeicoloViewModel = viewModel()
    val sessioneViewModel: SessioneViewModel = viewModel()
    val posizioneSalvataViewModel: PosizioneSalvataViewModel = viewModel()

    // Schermata iniziale: Home se già loggato, Login altrimenti
    val startDestination = if (utenteViewModel.sessionManager.isLoggedIn) {
        Destinazione.Home.rotta
    } else {
        Destinazione.Login.rotta
    }

    val username = utenteViewModel.sessionManager.usernameUtente

    val destinazioniPrincipali = listOf(
        Destinazione.Home,
        Destinazione.ListaVeicoli,
        Destinazione.Cronologia,
        Destinazione.Statistiche
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val destinazioneCorrente = navBackStackEntry?.destination?.route
    val mostraChrome = destinazioneCorrente != Destinazione.Login.rotta

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
                if (mostraChrome) {
                    BarraNavigazioneSuperiore(
                        onPosizioniSalvateClick = {
                            navController.navigate(Destinazione.PosizioniSalvate.rotta)
                        },
                        username = username,
                        onLogoutClick = {
                            // logout() aggiorna SessionManager.utenteCorrente = 0
                            // → tutti i Flow tornano vuoti automaticamente
                            utenteViewModel.logout()
                            navController.navigate(Destinazione.Login.rotta) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (mostraChrome) {
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
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding)
            ) {
                // ── LOGIN ────────────────────────────────────────────────
                composable(Destinazione.Login.rotta) {
                    SchermataLogin(
                        utenteViewModel = utenteViewModel,
                        onLoginSuccess = {
                            // SessionManager.utenteCorrente è già aggiornato con il nuovo ID.
                            // Tutti i Flow nei ViewModel si aggiornano automaticamente.
                            navController.navigate(Destinazione.Home.rotta) {
                                popUpTo(Destinazione.Login.rotta) { inclusive = true }
                            }
                        }
                    )
                }

                // ── HOME ─────────────────────────────────────────────────
                composable(Destinazione.Home.rotta) {
                    SchermataHome(
                        sessioneViewModel = sessioneViewModel,
                        veicoloViewModel = veicoloViewModel,
                        posizioneSalvataViewModel = posizioneSalvataViewModel
                    )
                }

                // ── LISTA VEICOLI ────────────────────────────────────────
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

                // ── AGGIUNGI VEICOLO ─────────────────────────────────────
                composable(Destinazione.AggiungiVeicolo.rotta) {
                    VeicoloFormScreen(
                        onIndietro = { navController.popBackStack() },
                        viewModel = veicoloViewModel
                    )
                }

                // ── MODIFICA VEICOLO ─────────────────────────────────────
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

                // ── POSIZIONI SALVATE ────────────────────────────────────
                composable(Destinazione.PosizioniSalvate.rotta) {
                    SchermataPosizioniSalvate(
                        onIndietro = { navController.popBackStack() },
                        viewModel = posizioneSalvataViewModel
                    )
                }

                // ── CRONOLOGIA ───────────────────────────────────────────
                composable(Destinazione.Cronologia.rotta) {
                    SchermataHistory(
                        sessioneViewModel = sessioneViewModel,
                        veicoloViewModel = veicoloViewModel
                    )
                }

                // ── STATISTICHE ──────────────────────────────────────────
                composable(Destinazione.Statistiche.rotta) {
                    SchermataStats(
                        sessioneViewModel = sessioneViewModel,
                        veicoloViewModel = veicoloViewModel
                    )
                }
            }
        }
    }
}
