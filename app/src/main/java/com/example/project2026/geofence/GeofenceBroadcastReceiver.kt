package com.example.project2026.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Errore geofencing: $errorMessage")
            return
        }

        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        for (geofence in triggeringGeofences) {
            Log.d(TAG, "Entrato nel geofence: ${geofence.requestId}")
            GeofenceNotificationHelper.inviaNotifica(context, geofence.requestId)
        }
    }

    companion object {
        private const val TAG = "GeofenceBroadcastReceiver"
    }
}