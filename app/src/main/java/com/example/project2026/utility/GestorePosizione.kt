package com.example.project2026.utility

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class GestorePosizione(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Recupera la posizione attuale del dispositivo.
     * Restituisce null se non è possibile ottenere la posizione o se mancano i permessi.
     */
    @SuppressLint("MissingPermission")
    suspend fun ottieniPosizioneAttuale(): Location? {
        return suspendCancellableCoroutine { continuation ->
            // Usiamo l'accuratezza bilanciata per risparmiare batteria, 
            // ma Priority.PRIORITY_HIGH_ACCURACY per il GPS preciso
            val cancellationTokenSource = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                continuation.resume(location)
            }.addOnFailureListener {
                continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }
}
