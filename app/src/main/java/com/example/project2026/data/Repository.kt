package com.example.project2026.data

import kotlinx.coroutines.flow.Flow

class Repository(
    private val veicoloDao: VeicoloDao,
    private val posizioneDao: PosizioneSalvataDao,
    private val sessioneDao: SessioneParcheggioDao
) {
    //Veicoli
    val tuttiIVeicoli: Flow<List<Veicolo>> = veicoloDao.ottieniTuttiIVeicoli()
    suspend fun inserisciVeicolo(veicolo: Veicolo)= veicoloDao.inserisciVeicolo(veicolo)
    suspend fun cancellaVeicolo(veicolo: Veicolo)= veicoloDao.cancellaVeicolo(veicolo)
    //Posizioni
    val tutteLePosizioni: Flow<List<PosizioneSalvata>> = posizioneDao.ottieniTutteLePosizioni()
    suspend fun inserisciPosizione(posizione: PosizioneSalvata)= posizioneDao.inserisciPosizione(posizione)

}
