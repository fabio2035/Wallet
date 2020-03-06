package com.example.wallet.data.repository

import androidx.lifecycle.LiveData
import com.example.wallet.data.dao.LocationDAO
import com.example.wallet.data.entity.DLocation


// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class LocationRepository(private val LocationDao: LocationDAO) {

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    val allLocations: List<DLocation> = LocationDao.getLocations()

    suspend fun insert(location: DLocation) {
        LocationDao.insert(location)
    }



}