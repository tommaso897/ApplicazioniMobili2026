package com.example.project2026.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val veicoloDao: VeicoloDao,
    private val posizioneDao: PosizioneSalvataDao,
    private val sessioneDao: SessioneParcheggioDao
) {
    // Veicoli - filtrati per utente
    fun tuttiIVeicoli(idUtente: Int): Flow<List<Veicolo>> = veicoloDao.ottieniTuttiIVeicoli(idUtente)
    suspend fun inserisciVeicolo(veicolo: Veicolo) = veicoloDao.inserisciVeicolo(veicolo)
    suspend fun cancellaVeicolo(veicolo: Veicolo) = veicoloDao.cancellaVeicolo(veicolo)
    suspend fun aggiornaVeicolo(veicolo: Veicolo) = veicoloDao.aggiornaVeicolo(veicolo)

    // Posizioni - filtrate per utente
    fun tutteLePosizioni(idUtente: Int): Flow<List<PosizioneSalvata>> = posizioneDao.ottieniTutteLePosizioni(idUtente)
    suspend fun inserisciPosizione(posizione: PosizioneSalvata) = posizioneDao.inserisciPosizione(posizione)
    suspend fun cancellaPosizione(posizione: PosizioneSalvata) = posizioneDao.cancellaPosizione(posizione)
}
