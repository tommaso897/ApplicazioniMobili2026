package com.example.project2026.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.project2026.data.AppDatabase
import com.example.project2026.data.SessioneParcheggio
import com.example.project2026.data.StatoParcheggio
import com.example.project2026.data.TipoParcheggio
import com.example.project2026.data.Veicolo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init {
        viewModelScope.launch {
            sessioneDao.ottieniSessioniAttive().collect {
                _sessioniAttive.value = it
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
        note: String? = null
    ) {
        viewModelScope.launch {
            val timestampInizio = System.currentTimeMillis()
            val dataInizio = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampInizio))

            // 1. Chiudo eventuali sessioni precedenti per questo veicolo
            sessioneDao.terminaSessioneAttivaPerVeicolo(veicolo.id, timestampInizio, dataInizio)

            // 2. Creo la nuova sessione
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
                stato = StatoParcheggio.PARCHEGGIATO,
                attivo = true
            )
            sessioneDao.salvaSessione(nuovaSessione)

            // 3. Aggiorno lo stato del veicolo
            val veicoloAggiornato = veicolo.copy(statoParcheggio = StatoParcheggio.PARCHEGGIATO)
            veicoloDao.aggiornaVeicolo(veicoloAggiornato)
        }
    }

    fun terminaParcheggio(sessione: SessioneParcheggio) {
        viewModelScope.launch {
            val timestampFine = System.currentTimeMillis()
            val dataFine = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.ITALY).format(Date(timestampFine))

            // Calcolo costo se è di tipo PAID (orario)
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

            // Aggiorno il veicolo riportandolo a LIBERO
            val veicolo = veicoloDao.ottieniVeicoloPerId(sessione.idVeicolo)
            veicolo?.let {
                veicoloDao.aggiornaVeicolo(it.copy(statoParcheggio = StatoParcheggio.LIBERO))
            }
        }
    }
}
