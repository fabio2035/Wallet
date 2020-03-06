package com.example.wallet.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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

import java.text.DateFormat
import java.util.*
import java.text.SimpleDateFormat
import android.util.Log
import androidx.room.Room
import com.example.wallet.data.Utility
import com.example.wallet.data.WalletRoomDatabase
import com.example.wallet.data.dao.LocationDAO
import com.example.wallet.data.entity.DLocation
import java.lang.Exception
import kotlin.math.abs

/**
 * Utility methods used in this sample.
 */
internal object LocationUtils {

    val KEY_LOCATION_UPDATES_REQUESTED = "location-updates-requested"
    val KEY_LOCATION_UPDATES_RESULT = "location-update-result"
    val CHANNEL_ID = "channel_01"

    var homewrkcnt: Int = 0
    var locationsCnt: Int = 0
    lateinit var prevLocation: List<DLocation>

    private val TAG = LocationUtils::class.java.simpleName

    private lateinit var locationDAO: LocationDAO

    private lateinit var db: WalletRoomDatabase

    private lateinit var tempString: StringBuilder

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
    fun sendNotification(context: Context, notificationDetails: String, contentTitle: String) {
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
        locationChecks(context, locations)


    }

    private fun locationChecks(
        context: Context,
        locations: List<Location>
    ) {

        db = Room.databaseBuilder(context, WalletRoomDatabase::class.java, "wallet.db").allowMainThreadQueries().build()


        //prep current datetime
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = Date()
        val dateStr = dateFormatter.format(date).toString()

        val locs = db.LocationDAO().getLocations()
            Log.i(TAG, "There are ${locs.count()} ")

            locs.forEach() {
                Log.i(TAG, it.toString())
            }
            tempString = java.lang.StringBuilder()
            //Check if there is any previous locations
            if (db.LocationDAO().getLocations().count() > 0) {
                prevLocation = db.LocationDAO().getPrevLoc()

                //check if device is at Home?
                if (deviceIsHome(locations)) {
                    //YES -> don't notify
                } else {
                    //NO - Device is in new location -> notify
                    when (checkLocDuration(dateStr, locations)) {
                        3 -> updateLocation(Utility.STATUS_HOME) //device has been in area long enough to be considered home or work place
                        2 -> {
                            updateLocation(Utility.STATUS_NOTIFIED)
                            sendNotification(context, "Did you make any purchase while in this location?", "Hello")
                             }//device is in new location - notify
                        1 -> {//notification already sent, do nothing
                             }
                        0 -> {
                            insertLocation(Utility.STATUS_NOTIFIED, locations, dateStr)
                            sendNotification(context, "Did you make any purchases recently?", "Hello")
                             }
                    }
                }
            } else {
                //Insert new location
                insertLocation(Utility.STATUS_LOCVER, locations, dateStr)
                sendNotification(context, "Hi. I'll be watching you from now on...", "Hello")
            }

        //close db
        if(db.isOpen){ db.close()}

        writeToScreen(tempString, context)

    }

    private fun updateLocation(status: String) {
        //update location with status
        Log.i(TAG, "updating location with id: " + prevLocation[0].id + " , status: " + status)
        try{
            tempString.append("Updating location with: $status").append("\n")
        db.LocationDAO().updateHomewrk(prevLocation[0].id, status)}catch (exc: Exception){
            Log.e(TAG, "Error trying to update record: " + exc.message)
        }
    }


    private fun insertLocation(
        status: String,
        locations: List<Location>,
        dateStr: String
    ) {
        try{
            tempString.append(dateStr).append("\n")
            tempString.append("inserting Lat: " + locations[0].latitude.toString() + " ; lng: " + locations[0].longitude.toString()).append("\n")
        Log.i(TAG, "inserting Lat: " + locations[0].latitude.toString() + " ; lng: " + locations[0].longitude.toString())
        val lat = locations[0].latitude.toString()
        val lng = locations[0].longitude.toString()

        db.LocationDAO().insert(DLocation(0,
            dateStr,
            lat,
            lng,
            status))}
        catch (exc: Exception){
            Log.e(TAG, "An error occured trying to insert data: " + exc.message)
        }

    }


    private fun checkLocDuration(
        dateStr: String,
        locations: List<Location>
    ): Int {
        //Integer to return
        var IntReturn = 0

        Log.i(TAG, "checking duration between crnt time and prev time")
        //check duration of current time compared to prev location time
        val date = Date()

        //check if there are any previous locations
        if(prevLocation.count()>0){

            //Is device within area of previous location?
            if(checkRadius(locations)){
                //check how long device has been in this area
                val dfr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val prevTime : Date = dfr.parse(prevLocation.get(0).date)
                val crntTime : Date = dfr.parse(dateStr)

                val diff : Long = crntTime.time - prevTime.time
                Log.i(TAG, "diff: $diff")
                //difference in minutes
                val diffMin = abs(diff / (60*1000))
                Log.i(TAG, "duration is: $diffMin")
                tempString.append(dateStr).append("\n")
                tempString.append("Duration is $diffMin compared to previous location").append("\n")

                IntReturn = when {
                    diffMin >= Utility.HOMEWRK_MINPERIOD -> //consider it home or work place
                        3
                    diffMin >= Utility.MIN_NOTIFICATION_PERIOD && prevLocation[0].status != Utility.STATUS_NOTIFIED  -> {
                        //send notification
                        2
                    }
                    diffMin >= Utility.MIN_NOTIFICATION_PERIOD && prevLocation[0].status == Utility.STATUS_NOTIFIED  -> {
                        //notification already sent, do nothing
                        1}
                    else -> 1 //do nothing
                }
            }else{
                tempString.append(dateStr).append("\n")
                tempString.append("It's new location setting LOCVER").append("\n")
                //It's a new area register it..
                IntReturn = 0
            }
        }else{
            tempString.append(dateStr).append("\n")
            tempString.append("It's new location setting LOCVER").append("\n")
            //there are no previous locations
            IntReturn = 0
        }

        return IntReturn
    }

    private fun checkRadius(locations: List<Location>): Boolean {

        var prevLoc = Location("prevlocation")
        prevLoc.latitude = prevLocation[0].lat.toDouble()
        prevLoc.longitude = prevLocation[0].lng.toDouble()


        var crntLocation = Location("crntLocation")
        crntLocation.latitude = locations[0].latitude
        crntLocation.longitude = locations[0].longitude

        val distance = crntLocation.distanceTo(prevLoc)

        Log.i(TAG, "distance is $distance")

        tempString.append("distance is $distance").append("\n")

        //if we find a previously known home/workplace location
        return distance <= Utility.MINIMUM_DISTANCE
    }


    private fun deviceIsHome(locations: List<Location>): Boolean {

        var returnVal = false
        var index = 0

        Log.i(TAG, "checking if device is at home or workplace...")
        var wrkLoc = db.LocationDAO().getHomewrk()
        if(wrkLoc.count()>0) {
            //if there are any previously marked home/workplace
            //compare current location with previously saved home/work location
            do {
                Log.i(
                    TAG,
                    "previous home lat: " + wrkLoc[index].lat + "; Long: " + wrkLoc[index].lng + "; date: " + wrkLoc[index].date + "; status: " + wrkLoc[index].status
                )
                var prevLoc = Location("prevlocation")
                prevLoc.latitude = wrkLoc[index].lat.toDouble()
                prevLoc.longitude = wrkLoc[index].lng.toDouble()


                var crntLocation = Location("crntLocation")
                crntLocation.latitude = locations[0].latitude
                crntLocation.longitude = locations[0].longitude

                Log.i(
                    TAG,
                    "current location lat: " + locations[0].latitude + " ;long: " + locations[0].longitude
                )

                val distance = crntLocation.distanceTo(prevLoc)

                //if we find a previously known home/workplace location
                if(distance <= Utility.MINIMUM_DISTANCE) {
                    returnVal =true
                    Log.i(TAG, "current location is at $distance from previously known home location")
                    tempString.append("Location is @ $distance from previous home location").append("\n")

                }
                index++
            } while(index <= wrkLoc.count() && !returnVal)


        }else{
            Log.i(TAG, "can't compare current location to home yet")
            tempString.append("Location is not close to home").append("\n")
            returnVal = false
        }

        return returnVal
    }

    private fun writeToScreen(text: StringBuilder, context: Context){
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(
                KEY_LOCATION_UPDATES_RESULT, text.toString()
            )
            .apply()
    }


    fun getLocationUpdatesResult(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(KEY_LOCATION_UPDATES_RESULT, "")
    }

}