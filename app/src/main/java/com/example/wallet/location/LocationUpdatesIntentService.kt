package com.example.wallet.location


import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.room.Room.databaseBuilder
import com.example.wallet.data.WalletRoomDatabase
import com.example.wallet.data.dao.LocationDAO
import com.example.wallet.viewmodel.LocationViewModel

import com.google.android.gms.location.LocationResult


/**
 * Handles incoming location updates and displays a notification with the location data.
 *
 * For apps targeting API level 25 ("Nougat") or lower, location updates may be requested
 * using [android.app.PendingIntent.getService] or
 * [android.app.PendingIntent.getBroadcast]. For apps targeting
 * API level O, only `getBroadcast` should be used.
 *
 * Note: Apps running on "O" devices (regardless of targetSdkVersion) may receive updates
 * less frequently than the interval specified in the
 * [com.google.android.gms.location.LocationRequest] when the app is no longer in the
 * foreground.
 */
class LocationUpdatesIntentService : IntentService(TAG) {


    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_PROCESS_UPDATES == action) {
                val result = LocationResult.extractResult(intent)
                if (result != null) {
                    val locations = result.locations
                    LocationUtils.setLocationUpdatesResult(this, locations)
                    //LocationUtils.sendNotification(this, LocationUtils.getLocationResultTitle(this, locations), "Location update")
                    Log.i(TAG, LocationUtils.getLocationUpdatesResult(this))
                }
            }
        }
    }

    companion object {

        private val ACTION_PROCESS_UPDATES =
            "com.example.wallet.location.action" + ".PROCESS_UPDATES"
        private val TAG = LocationUpdatesIntentService::class.java.simpleName
    }
}// Name the worker thread.