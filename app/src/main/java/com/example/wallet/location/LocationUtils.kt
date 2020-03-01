package com.example.wallet.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.example.wallet.MainActivity
import com.example.wallet.R
import com.example.wallet.data.DataContract

import java.text.DateFormat
import java.util.*
import android.widget.Toast
import com.firebase.ui.auth.AuthUI.getApplicationContext
import java.text.SimpleDateFormat
import android.util.Log

/**
 * Utility methods used in this sample.
 */
internal object LocationUtils {

    val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"
    val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
    val CHANNEL_ID = "channel_01"

    fun setRequestingLocationUpdates(context: Context, value: Boolean) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_LOCATION_UPDATES_REQUESTED, value)
            .apply()
    }

    fun getRequestingLocationUpdates(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(KEY_LOCATION_UPDATES_REQUESTED, false)
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    fun sendNotification(context: Context, notificationDetails: String) {
        // Create an explicit content Intent that starts the main Activity.
        val notificationIntent = Intent(context, MainActivity::class.java)

        notificationIntent.putExtra("from_notification", true)

        // Construct a task stack.
        val stackBuilder = TaskStackBuilder.create(context)

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity::class.java!!)

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent)

        // Get a PendingIntent containing the entire back stack.
        val notificationPendingIntent =
            stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        // Get a notification builder that's compatible with platform versions >= 4
        val builder = NotificationCompat.Builder(context)

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
            // In a real app, you may want to use a library like Volley
            // to decode the Bitmap.
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.mipmap.ic_launcher
                )
            )
            .setColor(Color.RED)
            .setContentTitle("Location update")
            .setContentText(notificationDetails)
            .setContentIntent(notificationPendingIntent)

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true)

        // Get an instance of the Notification manager
        val mNotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            // Create the channel for the notification
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            // Set the Notification Channel for the Notification Manager.
            mNotificationManager.createNotificationChannel(mChannel)

            // Channel ID
            builder.setChannelId(CHANNEL_ID)
        }

        // Issue the notification
        mNotificationManager.notify(0, builder.build())
    }


    /**
     * Returns the title for reporting about a list of [Location] objects.
     *
     * @param context The [Context].
     */
    fun getLocationResultTitle(context: Context, locations: List<Location>): String {
        val numLocationsReported = context.resources.getQuantityString(
            R.plurals.num_locations_reported, locations.size, locations.size
        )
        return numLocationsReported + ": " + DateFormat.getDateTimeInstance().format(Date())
    }

    /**
     * Returns te text for reporting about a list of  [Location] objects.
     *
     * @param locations List of [Location]s.
     */
    private fun getLocationResultText(context: Context, locations: List<Location>): String {
        if (locations.isEmpty()) {
            return context.getString(R.string.unknown_location)
        }
        val sb = StringBuilder()
        for (location in locations) {
            sb.append("(")
            sb.append(location.latitude)
            sb.append(", ")
            sb.append(location.longitude)
            sb.append(")")
            sb.append("\n")
        }
        return sb.toString()
    }

    fun setLocationUpdatesResult(context: Context, locations: List<Location>) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(
                KEY_LOCATION_UPDATES_RESULT, getLocationResultTitle(
                    context,
                    locations
                )
                        + "\n" + getLocationResultText(
                    context,
                    locations
                )
            )
            .apply()


        //Location algorithm
        locationChecks()


        //Save location updates in database...
        val cv = ContentValues()

        val dateFormatter = SimpleDateFormat("yyyyMMdd")
        val timeFormatter = SimpleDateFormat("HHmmss")
        val date = Date()

        //System.out.println(formatter.format(date))

        val dateStr = String.format(
            Locale.US,
            "%08d",
            Integer.parseInt(dateFormatter.format(date).toString())
        )

        val timeStr =
            String.format(Locale.US, "%04d", Integer.parseInt(timeFormatter.format(date).toString()));

        cv.put(DataContract.LocationEntry.COLUMN_DATE, dateStr)
        cv.put(DataContract.LocationEntry.COLUMN_TIME, timeStr)
        cv.put(DataContract.LocationEntry.COLUMN_LAT, locations.get(0).latitude)
        cv.put(DataContract.LocationEntry.COLUMN_LNG, locations.get(0).longitude)

        val uri = context.getContentResolver().insert(DataContract.LocationEntry.CONTENT_URI, cv)

        val execNum = DataContract.LocationEntry.getLocationFromUri(uri)

        Log.d("Location", "execNum = " + execNum)

        if (execNum!!.toInt() > 0) {
            Toast.makeText(
                context,
                "Something was added to database!",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun locationChecks() {

        //check if location is within home or work classified geofence

        if(checkHomeWork()){
            //Do notihing: device is at home or at work place
        } else{
            //Device is in new location, or simply not at home/work:
            //check if current location is within radius of previous location (class in 'LOCVER', 'NOTIFIED')
            if(checkRadius()){
                //Location is within radius of previous location register: check time difference, how long device has been in area
                when (checkLocDuration()) {
                    3 -> updateLocation('HOMEWRK') //device has been in area long enough to be considered home or work place
                    2 -> updateLocation('NOTIFIED') //device has been in area long enough to be sent a notification
                    else -> {} //Do noting: device has not been in area long enough for classification
                }
            }else{
                //device is in new location, out of previous 'LOCVER' or 'NOTIFIED' class: insert new location
                insertLocation('LOCVER')
            }
        }


    }

    private fun checkHomeWork(): Boolean {
    //check whether device is at home/work or in new location

    }

    fun getLocationUpdatesResult(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_LOCATION_UPDATES_RESULT, "")
    }
}