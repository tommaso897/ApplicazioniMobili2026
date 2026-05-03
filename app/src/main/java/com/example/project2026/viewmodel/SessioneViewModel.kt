package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.Veicolo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SessioneViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessioneDao = db.sessioneParcheggioDao()
    private val veicoloDao = db.veicoloDao()

    private val _sessioniAttive = MutableStateFlow<List<SessioneParcheggio>>(emptyList())
    val sessioniAttive: StateFlow<List<SessioneParcheggio>> = _sessioniAttive

    // Flow per la cronologia delle sessioni terminate
    val cronologiaTerminate: StateFlow<List<SessioneParcheggio>> = sessioneDao.ottieniCronologiaTerminate()
        .stateIn(
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
                _sessioniAttive.value.filter { it.tipo == TipoParcheggio.TICKET && it.scadenza != null }.forEach { sessione ->
                    if (oraAttuale >= (sessione.scadenza ?: Long.MAX_VALUE)) {
                        terminaParcheggio(sessione)
                    }
                }
                delay(1000)
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
        }
    }
}
