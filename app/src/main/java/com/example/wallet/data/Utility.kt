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