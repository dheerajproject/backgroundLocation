package com.dheeraj.backgroundlocation.apiService

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import androidx.annotation.StringDef
import com.dheeraj.backgroundlocation.R
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.hideProgressDialog
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.isInternetAvailable
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.showDialog
import com.dheeraj.backgroundlocation.controller.ApplicationClass.Companion.showProgressDialog
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class ApiService(@field:SuppressLint("StaticFieldLeak")
                 private var context: Context,
                 private var isLoadingIndicator: Boolean,
                 url: String,
                 @RequestedMethod method: String,
                 @ResponseType responseType: String,
                 private var postData: JSONObject?) :
    AsyncTask<String, Void, Int>() {

    private lateinit var serverResponseString: String
    private var serverResponseCode: Int = 0
    private var onSuccess: OnSuccess? = null
    private var onSuccessString: OnSuccessString? = null
    private var apiMethod: String? = method
    private var responseType: String? = responseType
    private var url: String? = url

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(RequestedMethod.GET, RequestedMethod.POST)
    annotation class RequestedMethod {
        companion object {
            const val POST = "POST"
            const val GET = "GET"
        }
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(ResponseType.STRING, ResponseType.JSON)
    annotation class ResponseType {
        companion object {
            const val STRING = "STRING"
            const val JSON = "JSON"
        }
    }

    interface OnSuccess {
        fun setOnSuccessListener(responseCode: Int?, response: JSONObject?)
    }
    interface OnSuccessString {
        fun setOnSuccessListener(responseCode: Int?, response: String)
    }

    fun setOnSuccessListener(onSuccess: OnSuccess) {
        this.onSuccess = onSuccess
    }

    fun setOnSuccessListenerString(onSuccessString: OnSuccessString) {
        this.onSuccessString = onSuccessString
    }

    override fun onPreExecute() {
        super.onPreExecute()

        if(isInternetAvailable(context)){
            if (isLoadingIndicator) {
                showProgressDialog(context as Activity)
            }
        }else{
            cancel(true)
        }
    }

    override fun doInBackground(vararg params: String): Int? {
        try {

            val urls = URL(url)

            val conn = urls.openConnection() as HttpURLConnection
            conn.readTimeout    = 60000 //milliseconds
            conn.connectTimeout = 60000 // milliseconds
            conn.requestMethod  = apiMethod
            conn.setRequestProperty("Content-Type", "application/json")

            conn.requestMethod

            if (postData != null && postData!!.length() > 0) {
                val printout = DataOutputStream(conn.outputStream)
                printout.writeBytes(postData!!.toString())
                printout.flush()
                printout.close()
                Log.d("api_post_data",postData.toString())
            }


            conn.connect()

            serverResponseCode   = conn.responseCode
            serverResponseString = readResponseFromServer(conn)

        } catch (e: Exception) {
            serverResponseString = ""
            Log.d("api_error", e.toString())
        }

        return serverResponseCode
    }

    override fun onPostExecute(integer: Int?) {
        super.onPostExecute(integer)

        try {
            if(responseType == ResponseType.STRING) {
                if(onSuccessString!= null)
                    onSuccessString!!.setOnSuccessListener(integer, serverResponseString)
            }else{
                if(onSuccess!= null)
                    onSuccess!!.setOnSuccessListener(integer, JSONObject(serverResponseString))
            }
        }catch (e:Exception){
            try {
                showDialog(context, context.getString(R.string.something_went_wrong))
            }catch (e:java.lang.Exception){ }
        }

        if(isLoadingIndicator)
            hideProgressDialog()
    }

    private fun readResponseFromServer(conn: HttpURLConnection): String {
        val response: String

        response = try {
            conn.inputStream.bufferedReader().use(BufferedReader::readText)
        } catch (e: Exception) {
            e.toString()
        }

        Log.d("api_response",response)

        return response
    }
}
