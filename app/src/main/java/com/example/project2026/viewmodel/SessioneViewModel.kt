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

class SessioneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessioneDao = db.sessioneParcheggioDao()
    private val veicoloDao = db.veicoloDao()
    private val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val parcheggioNotificationManager = ParcheggioNotificationManager(application, notificationManager)
    private val prefs = application.getSharedPreferences("sessione_parcheggio", Context.MODE_PRIVATE)

    private val _sessioniAttive = MutableStateFlow<List<SessioneParcheggio>>(emptyList())
    val sessioniAttive: StateFlow<List<SessioneParcheggio>> = _sessioniAttive

    // Traccia i timestamp di quando la notifica è stata mostrata per la prima volta (per evitare all'inizio)
    private val notificheAvviate = mutableMapOf<Int, Long>()

    // Flow per la cronologia delle sessioni terminate
    val cronologiaTerminate: StateFlow<List<SessioneParcheggio>> = sessioneDao.ottieniCronologiaTerminate()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val puntiHeatmap: StateFlow<List<CoordinateHeatmap>> = sessioneDao.getCoordinatePerHeatMap()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

     val statisticheSpese: StateFlow<List<SpesaVeicolo>> = sessioneDao.getSpesePerVeicolo()
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

     // StateFlow per le spese filtrate per data
     val statisticheSpeseFiltratePerData: StateFlow<List<SpesaVeicolo>> = _dataInizio.flatMapLatest { inizio ->
         _dataFine.flatMapLatest { fine ->
             sessioneDao.getSpesePerVeicoloFiltratoPerData(inizio, fine)
         }
     }.stateIn(
         scope = viewModelScope,
         started = SharingStarted.WhileSubscribed(5000),
         initialValue = emptyList()
     )

    init {
        viewModelScope.launch {
            sessioneDao.ottieniSessioniAttive().collect {
                _sessioniAttive.value = it
            }
        }

        viewModelScope.launch {
            while (true) {
                val oraAttuale = System.currentTimeMillis()
                
                // Aggiorna le notifiche ogni 5 secondi e controlla scadenze
                _sessioniAttive.value.forEach { sessione ->
                    // Controlla scadenza per ticket
                    if (sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null) {
                        if (oraAttuale >= (sessione.scadenza ?: Long.MAX_VALUE)) {
                            // Mostra notifica di scadenza prima di terminare
                            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                            veicolo?.let {
                                parcheggioNotificationManager.mostraNotificaScadenza(it.nome)
                            }
                            terminaParcheggio(sessione)
                        }
                    }

                    // Controlla se deve mostrare avviso di scadenza
                    if (deveMonstraAvvisoScadenza(sessione, oraAttuale)) {
                        val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                        veicolo?.let {
                            val tempoRimanente = formattaTempoTrascorso((sessione.scadenza ?: 0L) - oraAttuale)
                            parcheggioNotificationManager.mostraAvvisoScadenza(it.nome, tempoRimanente)
                        }
                    }

                    // Aggiorna notifica ogni 5 secondi ma SOLO dopo i primi 5 minuti E se non dismissata
                    if (sessione.attivo && !(sessione.tipo == TipoParcheggio.TICKET && sessione.scadenza != null && oraAttuale >= sessione.scadenza)) {
                        val minutiTrascorsi = (oraAttuale - sessione.inizio) / (1000 * 60)
                        
                        // Mostra notifica solo dopo 5 minuti E se non è stata dismissata
                        if (minutiTrascorsi >= 5 && !èNotificaDismissata(sessione.id)) {
                            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
                            veicolo?.let {
                                val tempoTrascorso = formattaTempoTrascorso(oraAttuale - sessione.inizio)
                                val costoAccumulato = calcolaCostoAccumulato(sessione, oraAttuale)

                                // Vibrazione ogni 5 minuti completi
                                val vibrare = minutiTrascorsi > 0 && minutiTrascorsi % 5 == 0L && notificheAvviate[sessione.id] != minutiTrascorsi

                                if (vibrare) {
                                    notificheAvviate[sessione.id] = minutiTrascorsi
                                }

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

                // Aspetta 5 secondi per avere tempo dinamico
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
            veicolo?.let {
                veicoloDao.aggiornaVeicolo(it.copy(statoParcheggio = StatoParcheggio.LIBERO))
            }

            // Elimina la notifica quando il parcheggio termina
            parcheggioNotificationManager.eliminaNotifica()
        }
    }

    /**
     * Formatta il tempo trascorso in HH:mm:ss
     */
    private fun formattaTempoTrascorso(milliseconds: Long): String {
        var secondi = milliseconds / 1000
        val ore = secondi / 3600
        secondi %= 3600
        val minuti = secondi / 60
        secondi %= 60

        return String.format("%02d:%02d:%02d", ore, minuti, secondi)
    }

    /**
     * Calcola il costo accumulato durante la sessione
     */
    private fun calcolaCostoAccumulato(sessione: SessioneParcheggio, oraAttuale: Long): String? {
        return when (sessione.tipo) {
            TipoParcheggio.PAID -> {
                val oreTrascorse = (oraAttuale - sessione.inizio) / (1000.0 * 60 * 60)
                val costo = oreTrascorse * (sessione.tariffa ?: 0.0)
                String.format("%.2f€", costo)
            }
            TipoParcheggio.TICKET -> sessione.costo?.let { String.format("%.2f€", it) }
            TipoParcheggio.FREE -> null
        }
    }

    /**
     * Verifica se la notifica è stata dismissata dall'utente
     * Ritorna true se è stata dismissata meno di 1 minuto fa
     */
    private fun èNotificaDismissata(sessioneId: Int): Boolean {
        val dismissedTime = prefs.getLong("dismissed_$sessioneId", 0)
        if (dismissedTime == 0L) return false

        val timeSinceDismiss = System.currentTimeMillis() - dismissedTime
        val oneMinute = 60 * 1000

        return timeSinceDismiss < oneMinute
    }

    /**
     * Registra che la notifica è stata dismissata
     */
    fun registraDismissioneNotifica(sessioneId: Int) {
        prefs.edit().putLong("dismissed_$sessioneId", System.currentTimeMillis()).apply()
    }

    /**
     * Programma una notifica di avviso per X minuti prima della scadenza
     */
    fun programmaAvvisoScadenza(sessioneId: Int, minutiAvviso: Int) {
        prefs.edit().putInt("avviso_minuti_$sessioneId", minutiAvviso).apply()
    }

    /**
     * Verifica se l'avviso è stato programmato e calcola se è il momento di mostrarlo
     */
    private fun deveMonstraAvvisoScadenza(sessione: SessioneParcheggio, oraAttuale: Long): Boolean {
        if (sessione.tipo != TipoParcheggio.TICKET || sessione.scadenza == null) return false

        val minutiAvviso = prefs.getInt("avviso_minuti_${sessione.id}", -1)
        if (minutiAvviso <= 0) return false

        val tempoAvviso = sessione.scadenza - (minutiAvviso * 60 * 1000L)
        val tempoFineAvviso = tempoAvviso + 30 * 1000L // Mostra per 30 secondi

        return oraAttuale in tempoAvviso..tempoFineAvviso && !èNotificaAvvisoDismissata(sessione.id)
    }

    /**
     * Verifica se la notifica di avviso è stata dismissata
     */
    private fun èNotificaAvvisoDismissata(sessioneId: Int): Boolean {
        val dismissedTime = prefs.getLong("dismissed_avviso_$sessioneId", 0)
        if (dismissedTime == 0L) return false

        val timeSinceDismiss = System.currentTimeMillis() - dismissedTime
        val oneMinute = 60 * 1000

        return timeSinceDismiss < oneMinute
    }

     /**
      * Registra che la notifica di avviso è stata dismissata
      */
     fun registraDismissioneAvviso(sessioneId: Int) {
         prefs.edit().putLong("dismissed_avviso_$sessioneId", System.currentTimeMillis()).apply()
     }

     /**
      * Imposta la data di inizio del filtro
      */
     fun setDataInizio(timestamp: Long) {
         _dataInizio.value = timestamp
     }

     /**
      * Imposta la data di fine del filtro
      */
     fun setDataFine(timestamp: Long) {
         _dataFine.value = timestamp
     }

     /**
      * Ritorna il timestamp dell'inizio di oggi (predefinito)
      */
     private fun getDataInizioPredefinita(): Long {
         val calendar = Calendar.getInstance()
         calendar.set(Calendar.HOUR_OF_DAY, 0)
         calendar.set(Calendar.MINUTE, 0)
         calendar.set(Calendar.SECOND, 0)
         calendar.set(Calendar.MILLISECOND, 0)
         return calendar.timeInMillis
     }

     /**
      * Ritorna il timestamp della fine di oggi (predefinito)
      */
     private fun getDataFinePredefinita(): Long {
         val calendar = Calendar.getInstance()
         calendar.set(Calendar.HOUR_OF_DAY, 23)
         calendar.set(Calendar.MINUTE, 59)
         calendar.set(Calendar.SECOND, 59)
         calendar.set(Calendar.MILLISECOND, 999)
         return calendar.timeInMillis
     }
}
