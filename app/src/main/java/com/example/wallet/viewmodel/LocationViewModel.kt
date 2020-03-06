package com.example.wallet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.wallet.data.WalletRoomDatabase
import com.example.wallet.data.entity.DLocation
import com.example.wallet.data.repository.LocationRepository
import kotlinx.coroutines.launch

// Class extends AndroidViewModel and requires application as a parameter.
class LocationViewModel(application: Application) : AndroidViewModel(application) {

    // The ViewModel maintains a reference to the repository to get data.
    private val repository: LocationRepository
    // LiveData gives us updated locations when they change.
    val allLocations: List<DLocation>

    init {
        // Gets reference to LocationDao from WalletRoomDatabase to construct
        // the correct LocationRepository.
        val locationDAO = WalletRoomDatabase.getDatabase(application).LocationDAO()
        repository = LocationRepository(locationDAO)
        allLocations = repository.allLocations
    }

    /**
     * The implementation of insert() in the database is completely hidden from the UI.
     * Room ensures that you're not doing any long running operations on
     * the main thread, blocking the UI, so we don't need to handle changing Dispatchers.
     * ViewModels have a coroutine scope based on their lifecycle called
     * viewModelScope which we can use here.
     */
    fun insert(location: DLocation) = viewModelScope.launch {
        repository.insert(location)
    }
}