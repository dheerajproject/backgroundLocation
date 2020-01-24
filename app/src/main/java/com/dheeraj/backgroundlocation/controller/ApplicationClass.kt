package com.dheeraj.backgroundlocation.controller

import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.dheeraj.backgroundlocation.R
import com.dheeraj.backgroundlocation.utils.DbHelper
import com.dheeraj.backgroundlocation.utils.SharedPref


class ApplicationClass : Application(){

    companion object{

        const val apiURL = "http://jaa.kozow.com/api/testing/data/"
        lateinit var dbHelper: DbHelper
        lateinit var sharedPref: SharedPref

        const val isServiceStart = "isServiceStart"

        private lateinit var dialog: AlertDialog

        @Suppress("DEPRECATION")
        fun isInternetAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val n = cm.activeNetwork
                if (n != null) {
                    val nc = cm.getNetworkCapabilities(n)
                    //It will check for both wifi and cellular network
                    return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(
                        NetworkCapabilities.TRANSPORT_WIFI)
                }
                showInternetDialog(context)
                return false
            } else {
                val netInfo = cm.activeNetworkInfo
                return netInfo != null && netInfo.isConnectedOrConnecting
            }
        }

        private fun showInternetDialog(context: Context) {

            val builder = AlertDialog.Builder(context)
            builder.setMessage(context.getString(R.string.gpsDisabled))
                .setCancelable(false)
                .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                    try {
                        ContextCompat.startActivity(
                            context,
                            Intent(Settings.ACTION_DATA_ROAMING_SETTINGS),
                            null
                        )
                    } catch (e: Exception) {
                    }
                }
                .setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog.cancel(); }
            val alert = builder.create()
            alert.show()
        }

        fun showDialog(context: Context, msg: String) {

            val builder = AlertDialog.Builder(context)
            dialog = builder.create()
            dialog.setMessage(msg)
            dialog.setCancelable(false)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        fun showProgressDialog(context: Context) {
            try {
                val builder = AlertDialog.Builder(context)
                builder.setView(R.layout.loading_view)
                dialog = builder.create()
                dialog.setCancelable(false)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
            }catch (e:Exception){}

        }

        fun hideProgressDialog(){
            if(::dialog.isInitialized && dialog.isShowing) dialog.dismiss()
        }
    }

    override fun onCreate() {
        super.onCreate()
        dbHelper = DbHelper(this)
        sharedPref = SharedPref(this)
    }
}