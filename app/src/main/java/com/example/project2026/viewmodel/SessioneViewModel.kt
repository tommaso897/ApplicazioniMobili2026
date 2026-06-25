package com.example.project2026.viewmodel

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.CoordinateHeatmap
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.SpesaVeicolo
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.Veicolo
import com.example.project2026.notification.ParcheggioNotificationManager
import com.example.project2026.utility.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class SessioneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessioneDao = db.sessioneParcheggioDao()
    private val veicoloDao = db.veicoloDao()
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val parcheggioNotificationManager = ParcheggioNotificationManager(application, notificationManager)
    private val prefs = application.getSharedPreferences("sessione_parcheggio", Context.MODE_PRIVATE)

    init {
        SessionManager(application)
    }

    private val _sessioniAttive = MutableStateFlow<List<SessioneParcheggio>>(emptyList())
    val sessioniAttive: StateFlow<List<SessioneParcheggio>> = _sessioniAttive

    // Traccia i timestamp di quando la notifica è stata mostrata per la prima volta
    private val notificheAvviate = mutableMapOf<Int, Long>()

    // ── FLOW REATTIVI: reagiscono automaticamente al login/logout ────────────

    val cronologiaTerminate: StateFlow<List<SessioneParcheggio>> = SessionManager.utenteCorrente
        .flatMapLatest { id -> sessioneDao.ottieniCronologiaTerminate(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val puntiHeatmap: StateFlow<List<CoordinateHeatmap>> = SessionManager.utenteCorrente
        .flatMapLatest { id -> sessioneDao.getCoordinatePerHeatMap(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val statisticheSpese: StateFlow<List<SpesaVeicolo>> = SessionManager.utenteCorrente
        .flatMapLatest { id -> sessioneDao.getSpesePerVeicolo(id) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // MutableStateFlow per i filtri data
    private val _dataInizio = MutableStateFlow(getDataInizioPredefinita())
    val dataInizio: StateFlow<Long> = _dataInizio

    private val _dataFine = MutableStateFlow(getDataFinePredefinita())
    val dataFine: StateFlow<Long> = _dataFine

    // Spese filtrate per data + utente (reattivo su tutti e tre i parametri)
    val statisticheSpeseFiltratePerData: StateFlow<List<SpesaVeicolo>> =
        SessionManager.utenteCorrente.flatMapLatest { id ->
            _dataInizio.flatMapLatest { inizio ->
                _dataFine.flatMapLatest { fine ->
                    sessioneDao.getSpesePerVeicoloFiltratoPerData(id, inizio, fine)
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Sessioni attive — reagiscono al cambio utente
        viewModelScope.launch {
            SessionManager.utenteCorrente.flatMapLatest { id ->
                sessioneDao.ottieniSessioniAttive(id)
            }.collect {
                _sessioniAttive.value = it
            }
        }

        // Loop notifiche ogni 5 secondi
        viewModelScope.launch {
            while (true) {
                val oraAttuale = System.currentTimeMillis()

                _sessioniAttive.value.forEach { sessione ->
                    // Controlla scadenza per ticket
                    if (sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null) {
                        if (oraAttuale >= (sessione.scadenza ?: Long.MAX_VALUE)) {
                            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                            veicolo?.let { parcheggioNotificationManager.mostraNotificaScadenza(it.nome) }
                            terminaParcheggio(sessione)
                        }
                    }

                    if (deveMonstraAvvisoScadenza(sessione, oraAttuale)) {
                        val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                        veicolo?.let {
                            val tempoRimanente = formattaTempoTrascorso((sessione.scadenza ?: 0L) - oraAttuale)
                            parcheggioNotificationManager.mostraAvvisoScadenza(it.nome, tempoRimanente)
                        }
                    }

                    if (sessione.attivo && !(sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null && oraAttuale >= sessione.scadenza)) {
                        val minutiTrascorsi = (oraAttuale - sessione.inizio) / (1000 * 60)
                        if (minutiTrascorsi >= 5 && !èNotificaDismissata(sessione.id)) {
                            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                            veicolo?.let {
                                val tempoTrascorso = formattaTempoTrascorso(oraAttuale - sessione.inizio)
                                val costoAccumulato = calcolaCostoAccumulato(sessione, oraAttuale)
                                val vibrare = minutiTrascorsi > 0 && minutiTrascorsi % 5 == 0L && notificheAvviate[sessione.id] != minutiTrascorsi
                                if (vibrare) notificheAvviate[sessione.id] = minutiTrascorsi
                                parcheggioNotificationManager.mostraNotifica(
                                    nomVeicolo = veicolo.nome,
                                    tipoVeicolo = veicolo.tipoVeicolo.name,
                                    tempoTrascorso = tempoTrascorso,
                                    costoAccumulato = costoAccumulato,
                                    sessionId = sessione.id,
                                    vibrare = vibrare
                                )
                            }
                        }
                    }
                }
                delay(5000)
            }
        }
    }

    fun iniziaParcheggio(
        veicolo: Veicolo,
        tipo: TipoParcheggio,
        tariffa: Double? = null,
        scadenza: Long? = null,
        costoIniziale: Double? = null,
        lat: Double? = null,
        lng: Double? = null,
        note: String? = null,
        foto: String? = null
    ) {
        viewModelScope.launch {
            val timestampInizio = System.currentTimeMillis()
            val dataInizio = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampInizio))

            sessioneDao.terminaSessioneAttivaPerVeicolo(veicolo.id, timestampInizio, dataInizio)

            val nuovaSessione = SessioneParcheggio(
                idUtente = SessionManager.utenteCorrente.value, // sempre aggiornato
                idVeicolo = veicolo.id,
                tipo = tipo,
                inizio = timestampInizio,
                dataInizio = dataInizio,
                tariffa = tariffa,
                scadenza = scadenza,
                costo = costoIniziale,
                latitudine = lat,
                longitudine = lng,
                note = note,
                foto = foto,
                stato = StatoParcheggio.PARCHEGGIATO,
                attivo = true
            )
            sessioneDao.salvaSessione(nuovaSessione)

            val veicoloAggiornato = veicolo.copy(statoParcheggio = StatoParcheggio.PARCHEGGIATO)
            veicoloDao.aggiornaVeicolo(veicoloAggiornato)
        }
    }

    fun terminaParcheggio(sessione: SessioneParcheggio) {
        viewModelScope.launch {
            val timestampFine = System.currentTimeMillis()
            val dataFine = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampFine))

            var costoFinale = sessione.costo ?: 0.0
            if (sessione.tipo == TipoParcheggio.PAID && sessione.tariffa != null) {
                val oreTrascorse = (timestampFine - sessione.inizio) / (1000.0 * 60 * 60)
                costoFinale = oreTrascorse * sessione.tariffa
            }

            val sessioneTerminata = sessione.copy(
                fine = timestampFine,
                dataFine = dataFine,
                costo = costoFinale,
                attivo = false,
                stato = StatoParcheggio.LIBERO
            )
            sessioneDao.aggiornaSessione(sessioneTerminata)

            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
            veicolo?.let { veicoloDao.aggiornaVeicolo(it.copy(statoParcheggio = StatoParcheggio.LIBERO)) }

            parcheggioNotificationManager.eliminaNotifica()
        }
    }

    private fun formattaTempoTrascorso(milliseconds: Long): String {
        var secondi = milliseconds / 1000
        val ore = secondi / 3600
        secondi %= 3600
        val minuti = secondi / 60
        secondi %= 60
        return String.format("%02d:%02d:%02d", ore, minuti, secondi)
    }

    private fun calcolaCostoAccumulato(sessione: SessioneParcheggio, oraAttuale: Long): String? {
        return when (sessione.tipo) {
            TipoParcheggio.PAID -> {
                val oreTrascorse = (oraAttuale - sessione.inizio) / (1000.0 * 60 * 60)
                String.format("%.2f€", oreTrascorse * (sessione.tariffa ?: 0.0))
            }
            TipoParcheggio.TICKET -> sessione.costo?.let { String.format("%.2f€", it) }
            TipoParcheggio.FREE -> null
        }
    }

    private fun èNotificaDismissata(sessioneId: Int): Boolean {
        val dismissedTime = prefs.getLong("dismissed_$sessioneId", 0)
        if (dismissedTime == 0L) return false
        return System.currentTimeMillis() - dismissedTime < 60 * 1000
    }

    fun registraDismissioneNotifica(sessioneId: Int) {
        prefs.edit().putLong("dismissed_$sessioneId", System.currentTimeMillis()).apply()
    }

    fun programmaAvvisoScadenza(sessioneId: Int, minutiAvviso: Int) {
        prefs.edit().putInt("avviso_minuti_$sessioneId", minutiAvviso).apply()
    }

    private fun deveMonstraAvvisoScadenza(sessione: SessioneParcheggio, oraAttuale: Long): Boolean {
        if (sessione.tipo != TipoParcheggio.TICKET || sessione.scadenza == null) return false
        val minutiAvviso = prefs.getInt("avviso_minuti_${sessione.id}", -1)
        if (minutiAvviso <= 0) return false
        val tempoAvviso = sessione.scadenza - (minutiAvviso * 60 * 1000L)
        val tempoFineAvviso = tempoAvviso + 30 * 1000L
        return oraAttuale in tempoAvviso..tempoFineAvviso && !èNotificaAvvisoDismissata(sessione.id)
    }

    private fun èNotificaAvvisoDismissata(sessioneId: Int): Boolean {
        val dismissedTime = prefs.getLong("dismissed_avviso_$sessioneId", 0)
        if (dismissedTime == 0L) return false
        return System.currentTimeMillis() - dismissedTime < 60 * 1000
    }

    fun registraDismissioneAvviso(sessioneId: Int) {
        prefs.edit().putLong("dismissed_avviso_$sessioneId", System.currentTimeMillis()).apply()
    }

    fun setDataInizio(timestamp: Long) { _dataInizio.value = timestamp }
    fun setDataFine(timestamp: Long) { _dataFine.value = timestamp }

    private fun getDataInizioPredefinita(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0); calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0); calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getDataFinePredefinita(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23); calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59); calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
