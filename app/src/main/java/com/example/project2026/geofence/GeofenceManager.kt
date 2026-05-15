package com.example.project2026.geofence

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.project2026.data.PosizioneSalvata
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    fun aggiungiGeofence(posizione: PosizioneSalvata) {
        val geofence = Geofence.Builder()
            .setRequestId(posizione.id.toString())
            .setCircularRegion(
                posizione.latitudine,
                posizione.longitudine,
                RAGGIO_METRI
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Log.d(TAG, "Geofence aggiunto: ${posizione.nome} (id=${posizione.id})")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore aggiunta geofence: ${e.message}")
            }
    }

    fun rimuoviGeofence(posizioneId: Int) {
        geofencingClient.removeGeofences(listOf(posizioneId.toString()))
            .addOnSuccessListener {
                Log.d(TAG, "Geofence rimosso: id=$posizioneId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Errore rimozione geofence: ${e.message}")
            }
    }

    fun rimuoviTuttiIGeofence() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    companion object {
        const val RAGGIO_METRI = 100f
        private const val TAG = "GeofenceManager"
    }
}