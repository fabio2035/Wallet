package com.example.wallet.data

import android.Manifest
import android.R
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.preference.PreferenceManager
import android.util.Log
import java.util.*

class Utility {

companion object{


    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    const val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    const val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds
    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     * Note: don't update locations faster than MIN_NOTIFICATION_PERIOD!!
     * Otherwise user is going to receive many notifications before it's registered as 'NOTIFIED'
     */
    const val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.
    const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

    /**
     * Minumum distance between previous and current location
     */
    const val MINIMUM_DISTANCE = 400
    /**
     * Minimum time period for device to be found in same location
     * to be considered home or workplace
     */
    const val HOMEWRK_MINPERIOD = 180
    /**
     * Minimum time to send notification (in minutes)
     */
    const val MIN_NOTIFICATION_PERIOD = 5

    const val STATUS_HOME = "HOMEWRK"

    const val STATUS_LOCVER = "LOCVER"

    const val STATUS_NOTIFIED = "NOTIFIED"


    fun getStatsNavYear(c: Context): Int {
        val cal = Calendar.getInstance()
        val year = cal.get(Calendar.YEAR)

        val prefs = PreferenceManager.getDefaultSharedPreferences(c)
        return prefs.getInt(
            c.getString(com.example.wallet.R.string.pref_stats_year_status),
            year
        )
    }

}


}