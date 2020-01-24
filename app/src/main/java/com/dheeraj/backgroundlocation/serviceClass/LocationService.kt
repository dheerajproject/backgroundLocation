package com.dheeraj.backgroundlocation.serviceClass

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.dbHelper
import com.dheeraj.backgroundlocation.model.LocationModel

class LocationService : Service() {
    private var mLocationManager: LocationManager? = null

    private var mLocationListeners = arrayOf(LocationListener(this,LocationManager.GPS_PROVIDER),
        LocationListener(this, LocationManager.NETWORK_PROVIDER))

    class LocationListener(context: Context,provider: String) : android.location.LocationListener {
        private var mLastLocation: Location

        private var broadcaster: LocalBroadcastManager? = null

        init {
            Log.e(TAG, "LocationListener $provider")
            mLastLocation = Location(provider)
            broadcaster = LocalBroadcastManager.getInstance(context)
        }

        override fun onLocationChanged(location: Location) {
            Log.e(TAG, "onLocationChanged: $location")
            mLastLocation.set(location)
            Log.v("LastLocation", mLastLocation.latitude.toString() +"  " + mLastLocation.longitude.toString())

            with(LocationModel()){
                lat     = mLastLocation.latitude
                longi   = mLastLocation.longitude
                timestamp = System.currentTimeMillis()

                val intent = Intent("notify")
                intent.putExtra("lat",lat)
                intent.putExtra("longi",longi)

                broadcaster!!.sendBroadcast(intent)
                dbHelper.addLocation(this)
            }
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(TAG, "onProviderDisabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(TAG, "onProviderEnabled: $provider")
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(TAG, "onStatusChanged: $provider")
        }
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onCreate() {
        Log.e(TAG, "onCreate")
        initializeLocationManager()
        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[1])
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "network provider does not exist, " + ex.message)
        }

        try {
            mLocationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_INTERVAL.toLong(), LOCATION_DISTANCE,
                mLocationListeners[0])
        } catch (ex: SecurityException) {
            Log.i(TAG, "fail to request location update, ignore", ex)
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, "gps provider does not exist " + ex.message)
        }
    }

    override fun onDestroy() {
        Log.e(TAG, "onDestroy")
        super.onDestroy()
        if (mLocationManager != null) {
            for (i in mLocationListeners.indices) {
                try {
                    mLocationManager!!.removeUpdates(mLocationListeners[i])
                } catch (ex: Exception) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex)
                }
            }
        }
    }

    private fun initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager")
        if (mLocationManager == null) {
            mLocationManager = applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
    }

    companion object {
        private const val TAG = "GPS"
        private const val LOCATION_INTERVAL = 1000
        private const val LOCATION_DISTANCE = 1f
    }
}