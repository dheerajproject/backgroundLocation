package com.dheeraj.backgroundlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dheeraj.backgroundlocation.adapter.LocationAdapter
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.dbHelper
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.isServiceStart
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.sharedPref
import com.dheeraj.backgroundlocation.interfaces.OnItemClickListener
import com.dheeraj.backgroundlocation.model.LocationModel
import com.dheeraj.backgroundlocation.serviceClass.LocationService
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), OnItemClickListener {

    private val permissionRequest = 2002
    private lateinit var locationArrayList:ArrayList<LocationModel>
    private lateinit var headerMenu: Menu

    private lateinit var alert: AlertDialog

    override fun onResume() {
        super.onResume()
        checkOrAskLocationPermission()
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mOnlineStatusReceiver,
            IntentFilter("notify")
        )
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mOnlineStatusReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showLocationData()
    }

    private fun showLocationData() {
        locationArrayList = dbHelper.getLocationArrayList()
        if (locationArrayList.size > 0) {
           setAdapter()
        }else{
            locationRecycler.visibility = GONE
            tvNoData.visibility         = VISIBLE
        }
    }

    private fun setAdapter() {
        locationRecycler.visibility = VISIBLE
        tvNoData.visibility         = GONE

        locationArrayList.let {
            val locationAdapter     = LocationAdapter(it, this)
            locationRecycler.adapter= locationAdapter
        }
    }

    private fun startLocationService() {
        startService(Intent(this, LocationService::class.java))
    }

    private fun checkOrAskLocationPermission():Boolean {
        // Check GPS is enabled
        if(!isGPSon()) return false

        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permission == PackageManager.PERMISSION_GRANTED) {
            return true
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                permissionRequest
            )
        }
        return false
    }

    private fun isGPSon():Boolean{
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(this)
            false
        }else{
            true
        }
    }

    private fun buildAlertMessageNoGps(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.gpsDisabled))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.yes)) { _, _ -> context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.cancel(); }
        alert = builder.create()
        alert.show()
    }


    @SuppressLint("SetTextI18n")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        headerMenu = menu
        if(sharedPref.getBoolean(isServiceStart)){
            headerMenu.getItem(0).isVisible = true
            headerMenu.getItem(1).isVisible = false
        }else{
            headerMenu.getItem(0).isVisible = false
            headerMenu.getItem(1).isVisible = true

            tvNoData.text = tvNoData.text.toString()+"\n\n"+getString(R.string.startServiceMsg)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_stop -> {
                sharedPref.putBoolean(isServiceStart,false)
                stopService(Intent(this, LocationService::class.java))
                Toast.makeText(this,getString(R.string.service_stopped),Toast.LENGTH_SHORT).show()

                headerMenu.getItem(0).isVisible = false
                headerMenu.getItem(1).isVisible = true
            }

            R.id.action_start -> {
                sharedPref.putBoolean(isServiceStart,true)
                startLocationService()
                Toast.makeText(this,getString(R.string.service_start),Toast.LENGTH_SHORT).show()

                headerMenu.getItem(0).isVisible = true
                headerMenu.getItem(1).isVisible = false
            }

            R.id.action_clear -> {
                locationArrayList.clear()

                dbHelper.clearLocationData()

                locationRecycler.visibility = GONE
                tvNoData.visibility = VISIBLE
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequest) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, getString(R.string.permission), Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClick(position: Int, modelData: Any) {

    }

    private val mOnlineStatusReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val newLat     = intent.getDoubleExtra("lat", 0.0)
            val newLongi   = intent.getDoubleExtra("longi", 0.0)

            with(LocationModel()){
                lat         = newLat
                longi       = newLongi
                timestamp   = System.currentTimeMillis()

                locationArrayList.add(this)
            }
            if(locationArrayList.size==1){
                setAdapter()
            }else
                locationRecycler.adapter!!.notifyItemInserted(locationArrayList.size-1)
        }
    }


    override fun onPause() {
        super.onPause()
        if(::alert.isInitialized && alert.isShowing) alert.dismiss()
    }
}
