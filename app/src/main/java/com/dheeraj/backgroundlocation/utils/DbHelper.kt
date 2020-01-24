package com.dheeraj.backgroundlocation.utils

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.AsyncTask
import android.util.Log
import com.dheeraj.backgroundlocation.apiService.ApiService
import com.dheeraj.backgroundlocation.apiService.ApiService.RequestedMethod.Companion.POST
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.apiURL
import com.dheeraj.backgroundlocation.model.LocationModel
import org.json.JSONObject

class DbHelper(private val context: Context): SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object{
        private const val DATABASE_VERSION  = 1
        private const val DATABASE_NAME     = "locationDatabase.db"
        private const val LOCATION_TABLE    = "locationTable"
        private const val KEY_ID            = "id"
        private const val KEY_LATITUDE      = "latitude"
        private const val KEY_LONGITUDE     = "longitude"
        private const val KEY_TIMESTAMP     = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        //create question table
        val createQuestionTableQuery = "CREATE TABLE $LOCATION_TABLE (" +
                "$KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$KEY_LATITUDE DECIMAL(11,7)," +
                "$KEY_LONGITUDE DECIMAL(11,7)," +
                "$KEY_TIMESTAMP TIMESTAMP" +
                ")"
        db!!.execSQL(createQuestionTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $LOCATION_TABLE")
        onCreate(db)
    }

    fun addLocation(locationModel: LocationModel){

        val postData = JSONObject()
        postData.put(KEY_ID,2)
        postData.put(KEY_LATITUDE,locationModel.lat)
        postData.put(KEY_LONGITUDE,locationModel.longi)

        val apiService = ApiService(context, false, apiURL, POST, ApiService.ResponseType.JSON,postData)
        apiService.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        apiService.setOnSuccessListener(object : ApiService.OnSuccess {
            override fun setOnSuccessListener(responseCode: Int?, response: JSONObject?) {
                Log.d("API_RESPONSE",response.toString())
            }
        })

        val db = this.writableDatabase

        val contentValues = ContentValues()
        contentValues.put(KEY_LATITUDE,locationModel.lat)
        contentValues.put(KEY_LONGITUDE,locationModel.longi)
        contentValues.put(KEY_TIMESTAMP,locationModel.timestamp)

        db.insert(LOCATION_TABLE,"", contentValues)
        db.close()
    }

    fun getLocationArrayList(): ArrayList<LocationModel>{
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT * FROM $LOCATION_TABLE", null)

        val locationArrayList = ArrayList<LocationModel>()

        while (cursor.moveToNext()){
            val locationModel = LocationModel()

            with(locationModel){
                lat      = cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE))
                longi    = cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE))
                timestamp= cursor.getLong(cursor.getColumnIndex(KEY_TIMESTAMP))

                locationArrayList.add(this)
            }
        }

        cursor.close()
        db.close()
        return locationArrayList
    }

    fun clearLocationData(){
        val db = this.writableDatabase
        db!!.execSQL("DELETE FROM $LOCATION_TABLE")
    }
}