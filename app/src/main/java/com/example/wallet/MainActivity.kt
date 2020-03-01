package com.example.wallet

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
//import com.example.wallet.databinding.ActivityMainBinding
import com.example.wallet.ui.PrivacyPolicy
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.IntentSender
import android.content.SharedPreferences
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.OnSuccessListener
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.databinding.library.BuildConfig
import com.example.wallet.location.LocationUpdatesBroadcastReceiver
import com.example.wallet.location.LocationUtils
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.snackbar.Snackbar
import java.util.*
//import android.widget.Button
//import android.widget.TextView

class MainActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {


    private val TAG = MainActivity::class.java.simpleName

    //lateinit var mainBinding: ActivityMainBinding

    //FireBase Authentication variables
    lateinit var mFirebaseAuth: FirebaseAuth
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener
    var RC_SIGN_IN: Int = 1

    //Location variables
    /**
     * Provides access to the Fused Location Provider API.
     */
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    lateinit var mLocationRequest: LocationRequest
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.
    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    private val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds
    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
    private val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.
    private val REQUEST_PERMISSIONS_REQUEST_CODE = 34


    // UI Widgets.
    lateinit var mRequestUpdatesButton: Button
    lateinit var mRemoveUpdatesButton: Button
    lateinit var mLocationUpdatesResultView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //mainBinding = setContentView(this, R.layout.activity_main)

        mRequestUpdatesButton = findViewById(R.id.request_updates_button) as Button
        mRemoveUpdatesButton = findViewById(R.id.remove_updates_button) as Button
        mLocationUpdatesResultView = findViewById(R.id.location_updates_result) as TextView

        //Authenticate user through firebase
        setFireBaseAuthentication()

        //Check if we have permission for location functions
        if (!checkPermissions()) {
            requestPermissions()
        }

        getUserLocation()

    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(): Boolean {
        val fineLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        )

        val backgroundLocationPermissionState = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )

        return fineLocationPermissionState == PackageManager.PERMISSION_GRANTED && backgroundLocationPermissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {

        val permissionAccessFineLocationApproved = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val backgroundLocationPermissionApproved = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val shouldProvideRationale =
            permissionAccessFineLocationApproved && backgroundLocationPermissionApproved

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.")
            Snackbar.make(
                findViewById(R.id.activity_main),
                R.string.permission_rationale,
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(R.string.ok, View.OnClickListener {
                    // Request permission
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        ),
                        REQUEST_PERMISSIONS_REQUEST_CODE
                    )
                })
                .show()
        } else {
            Log.i(TAG, "Requesting permission")
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.")

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Calling requestLocationUpdates")
                // Permission was granted.
                requestLocationUpdates(null)

            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                Snackbar.make(
                    findViewById(R.id.activity_main),
                    R.string.permission_denied_explanation,
                    Snackbar.LENGTH_INDEFINITE
                )
                    .setAction(R.string.settings, View.OnClickListener {
                        // Build intent that displays the App settings screen.
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts(
                            "package",
                            BuildConfig.APPLICATION_ID, null
                        )
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    })
                    .show()
            }
        }
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    fun requestLocationUpdates(view: View?) {
        try {
            Log.i(TAG, "Starting location updates")
            LocationUtils.setRequestingLocationUpdates(this, true)
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, getPendingIntent())
        } catch (e: SecurityException) {
            LocationUtils.setRequestingLocationUpdates(this, false)
            e.printStackTrace()
            Log.d(TAG, "Error processing location updates")
        }

    }

    private fun getPendingIntent(): PendingIntent {
        // Note: for apps targeting API level 25 ("Nougat") or lower, either
        // PendingIntent.getService() or PendingIntent.getBroadcast() may be used when requesting
        // location updates. For apps targeting API level O, only
        // PendingIntent.getBroadcast() should be used. This is due to the limits placed on services
        // started in the background in "O".

        // TODO(developer): uncomment to use PendingIntent.getService().
        //        Intent intent = new Intent(this, LocationUpdatesIntentService.class);
        //        intent.setAction(LocationUpdatesIntentService.ACTION_PROCESS_UPDATES);
        //        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun setFireBaseAuthentication() {
        mFirebaseAuth = FirebaseAuth.getInstance()

        mAuthStateListener = object : FirebaseAuth.AuthStateListener {

            override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {

                var user = firebaseAuth.currentUser

                if (user != null) {
                    //user is signed in
                    Toast.makeText(
                        this@MainActivity,
                        "You're now signed in. Welcome!",
                        Toast.LENGTH_LONG
                    ).show()
                    onSignedInInitialize(user)
                } else {
                    //user is signed out
                    onSignedOutCleanup()
                    // Choose authentication providers
                    val providers = arrayListOf(
                        AuthUI.IdpConfig.EmailBuilder().build(),
                        AuthUI.IdpConfig.GoogleBuilder().build()
                    )

                    // Create and launch sign-in intent
                    startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(providers)
                            .setLogo(R.drawable.logo_1_main)
                            .setTheme(R.style.FirebaseUITheme)
                            .build(),
                        RC_SIGN_IN
                    )
                }
            }

        }
    }

    private fun getUserLocation() {

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        mLocationRequest.setInterval(UPDATE_INTERVAL)


        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL)


        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME)

    }

/*
        Places.initialize(getApplicationContext(), getString(R.string.googlePlacesAPI))

        var placesClient = Places.createClient(this);

        //if(isPermissionGranted()) {
        Log.d(TAG, "retrieving location info..")
        //Check for permission to get userlocation
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "This permission needs explanation!",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1
                )
            }
        } else {
            //We have permission to execute location check..

            var placeFields = Collections.singletonList(Place.Field.NAME)

            // Use the builder to create a FindCurrentPlaceRequest.
            var request =  FindCurrentPlaceRequest.newInstance(placeFields)

            var placeResponse = placesClient.findCurrentPlace(request)

            placeResponse.addOnCompleteListener { task-> if (task.isSuccessful()) {
                val response = task.getResult()
                for (placeLikelihood in response!!.getPlaceLikelihoods()) {
                    Log.i("Location", String.format("Place: '%s'; likelihood: %f; Type: %s",
                        placeLikelihood.getPlace().getName(),
                        placeLikelihood.getLikelihood(),
                        placeLikelihood.getPlace().getTypes()))
                }
            } else {
                val exception = task.getException()
                if (exception is ApiException) {
                    val apiException = exception as ApiException
                    Log.e("Location", "Place not found: " + apiException.getStatusCode())
                }
            } }

        }



        }
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            Log.d("location atributes", "Went through missing permission atributes")
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location : android.location.Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d("location atributes", location.latitude.toString())
                    }
                }

        //setup a location request
        val locationRequest = LocationRequest.create()?.apply {
            interval = 20000
            fastestInterval = 20000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        //Get current location settings
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest!!)
        //Check whether current location settings are satisfied
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            Log.d("Location", locationSettingsResponse.toString())
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException){
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(this@MainActivity,
                        1)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }*/


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this@MainActivity, "Signed In!", Toast.LENGTH_LONG).show()
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this@MainActivity, "Sign in canceled", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun onSignedOutCleanup() {
        //detach listeners cleanup
    }

    private fun onSignedInInitialize(user: FirebaseUser) {
        //functionality for when user is signed in
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_signOut ->
                //Sign Out
            {
                AuthUI.getInstance().signOut(this)
                true
            }
            R.id.action_disclaimer -> {
                OpenPrivacyPolicy()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun OpenPrivacyPolicy() {
        // Opens the privacy policy activity
        val intent = Intent(
            applicationContext,
            PrivacyPolicy::class.java
        )
        startActivity(intent)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    override fun onPause() {
        super.onPause()
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
        mLocationUpdatesResultView.setText(LocationUtils.getLocationUpdatesResult(this))
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)
    }


    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(p0: SharedPreferences?, s: String?) {
        if (s == LocationUtils.KEY_LOCATION_UPDATES_RESULT) {
            mLocationUpdatesResultView.setText(LocationUtils.getLocationUpdatesResult(this))
        } else if (s == LocationUtils.KEY_LOCATION_UPDATES_REQUESTED) {
            //updateButtonsState(Utils.getRequestingLocationUpdates(this))
        }
    }

    /**
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    fun removeLocationUpdates(view: View) {
        Log.i(TAG, "Removing location updates")
        LocationUtils.setRequestingLocationUpdates(this, false)
        mFusedLocationClient.removeLocationUpdates(getPendingIntent())
    }


    /*private fun isPermissionGranted() : Boolean {
        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
         if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
             Toast.makeText(this@MainActivity, "This permission needs explanation!", Toast.LENGTH_LONG).show()
         }   else{
             ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),1)
         }else{
                return true
            }
        }
    }*/
}