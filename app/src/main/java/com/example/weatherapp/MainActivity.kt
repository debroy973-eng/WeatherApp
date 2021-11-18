package com.example.weatherapp

import android.Manifest
import android.Manifest.permission.*
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.example.weatherapp.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationSettingsRequest.Builder
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_INDEFINITE
import com.google.android.material.snackbar.Snackbar
import org.json.JSONException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private val mHourAdapter = WeatherAdapter(ArrayList())
    private val mDayAdapter = WeatherAdapter(ArrayList())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var lat:Double = 28.65381      //place holder data
    private var lng:Double = 77.22897      //place holder data
    lateinit var mTemperature:TextView
    lateinit var mPlace:TextView
    lateinit var mDate:TextView
    lateinit var mTime:TextView
    lateinit var mDescription:TextView
    lateinit var progressDays:ProgressBar
    lateinit var progressMain:ProgressBar
    lateinit var progressHours:ProgressBar
    lateinit var mImageMain:ImageView
    private val LOCATION_REQUEST_INTERVAL:Long=5000
    //private lateinit var locationManager: LocationManager
    //private lateinit var googleApiClient:GoogleApiClient

    private var mLocationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            lat  = locationResult.lastLocation.latitude
            lng = locationResult.lastLocation.longitude
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val locationManager= getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ) {
            Log.e("permission", "denied")
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                34
            )
        }
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            alertBuilder()
        }
        if(ActivityCompat.checkSelfPermission(this, ACCESS_NETWORK_STATE)!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(CHANGE_NETWORK_STATE, ACCESS_NETWORK_STATE),34)
        }
        val connectivityManager=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=connectivityManager.activeNetwork
        if(networkInfo==null){
            val builder=AlertDialog.Builder(this)
            builder.setMessage(getString(R.string.internet_enable_message))
                .setPositiveButton(getString(R.string.enable),DialogInterface.OnClickListener { dialogInterface, i ->
                    startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
                })
                .create()
                .show()
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        val recyclerViewForHours = binding.hoursUpdateRecyclerView
        val recyclerViewForDays = binding.daysUpdateRecyclerView
        recyclerViewForDays.layoutManager = LinearLayoutManager(this)
        recyclerViewForHours.layoutManager = LinearLayoutManager(this)
        mTemperature = binding.temperatureTextview
        mPlace = binding.placeTextview
        mDate = binding.dateTextview
        mTime = binding.timeTextview
        val mSwipeRefreshLayout=binding.swipeRefreshParent
            .setOnRefreshListener {
                fetchDataForCurrent()
            }
        mDescription = binding.descriptionTextview
        progressMain=binding.mainProgressBar
        progressHours=binding.hoursProgressBar
        progressDays=binding.daysProgressBar
        mImageMain=binding.imageMain
        mDayAdapter.getActivityContext(this)
        mHourAdapter.getActivityContext(this)
        fusedLocationClient.lastLocation.addOnSuccessListener {location->
            try {
                lat = location.latitude
                lng = location.longitude
            }catch (exception:Exception){
                Log.e("Location", exception.printStackTrace().toString())
            }
            fetchDataForCurrent(/*temperature, place, date, time,
                description,progressMain,progressDays,progressHours,imageView1*/)
        }
        recyclerViewForDays.adapter = mDayAdapter
        recyclerViewForHours.adapter = mHourAdapter
    }
    private fun alertBuilder(){
        val builder=AlertDialog.Builder(this)
        builder.setMessage(R.string.alertdialog_message)
            .setPositiveButton(getString(R.string.enable),DialogInterface.OnClickListener(){ dialog, id->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            })
            .create()
            .show()
    }
    private fun fetchDataForCurrent(
        /*tempTextview: TextView,
        placeTextView: TextView,
        dateTextView: TextView,
        timeTextView: TextView,
        descriptionTextView: TextView,
        progressMain:ProgressBar,
        progressDays:ProgressBar,
        progressHours:ProgressBar,
        imageView: ImageView*/
    ) {
        val url =
            "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lng&appid=75bc2a430b7eddd29511028df17b7fbb"
        val hourData = ArrayList<WeatherData>()
        val dayData = ArrayList<WeatherData>()
        Snackbar.make(
            findViewById(R.id.my_Constraint_Layout),
            R.string.snackbar_show,
            Snackbar.LENGTH_LONG
        ).show()
        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val jsonObject1 = response.getJSONObject("current")
                    val place = response.getString("timezone")
                    val unformattedDate1 = jsonObject1.getLong("dt")
                    val temp1 = jsonObject1.getDouble("temp")
                    val jsonArray1 = jsonObject1.getJSONArray("weather")
                    val obj1 = jsonArray1.getJSONObject(0)
                    val description1 = obj1.getString("description")
                    val icon=obj1.getString("icon")
                    val stringBuffer = StringBuffer()
                    progressMain.visibility=View.GONE
                    mTemperature.text =
                        stringBuffer.append(Math.floor(temp1 - 273).toInt()).append("\u2103").toString()
                    mPlace.text = place
                    mDescription.text = description1
                    mTime.text = formatTime(unformattedDate1 * 1000)
                    mDate.text = formatDate(unformattedDate1 * 1000)
                    /*Glide.with(this)
                        .load("http://openweathermap.org/img/wn/"+icon+"@2x.png")
                        .override(73,66)
                        .centerCrop()
                        .into(imageView)*/
                    when(icon){
                        "01d"->mImageMain.setImageResource(R.drawable.clear_sky_morning)
                        "01d"->mImageMain.setImageResource(R.drawable.clear_sky_night)
                        "02d"->mImageMain.setImageResource(R.drawable.few_clouds_morning)
                        "02n"->mImageMain.setImageResource(R.drawable.few_clouds_night)
                        "03d"->mImageMain.setImageResource(R.drawable.scattered_clouds)
                        "03n"->mImageMain.setImageResource(R.drawable.scattered_clouds)
                        "04d"->mImageMain.setImageResource(R.drawable.broken_clouds)
                        "04n"->mImageMain.setImageResource(R.drawable.broken_clouds)
                        "09d"->mImageMain.setImageResource(R.drawable.shower_rain)
                        "09n"->mImageMain.setImageResource(R.drawable.shower_rain)
                        "10d"->mImageMain.setImageResource(R.drawable.rain_morning)
                        "10n"->mImageMain.setImageResource(R.drawable.rain_night)
                        "11d"->mImageMain.setImageResource(R.drawable.thunderstorm)
                        "11n"->mImageMain.setImageResource(R.drawable.thunderstorm)
                        "13d"->mImageMain.setImageResource(R.drawable.snow)
                        "13n"->mImageMain.setImageResource(R.drawable.snow)
                        "50d"->mImageMain.setImageResource(R.drawable.mist)
                        else->mImageMain.setImageResource(R.drawable.mist)
                    }
                    val jsonHourlyArray = response.getJSONArray("hourly")
                    for (i in 0 until jsonHourlyArray.length()) {
                        val jsonObject = jsonHourlyArray.getJSONObject(i)
                        val unformattedDate = jsonObject.getLong("dt")
                        val temp = jsonObject.getDouble("temp")
                        val jsonArray = jsonObject.getJSONArray("weather")
                        val obj = jsonArray.getJSONObject(0)
                        val description = obj.getString("description")
                        val iconHour=obj.getString("icon")
                        val sb = StringBuilder()
                        hourData.add(
                            WeatherData(
                                sb.append(Math.floor(temp - 273).toInt())
                                    .append("\u2103").toString(),
                                description, formatDate(unformattedDate * 1000),
                                formatTime(unformattedDate * 1000),
                                iconHour
                            )
                        )
                    }
                    mHourAdapter.updateData(hourData,progressHours)
                    val jsonDailyArray = response.getJSONArray("daily")
                    for (i in 0 until jsonDailyArray.length()) {
                        val jsonObject = jsonDailyArray.getJSONObject(i)
                        val unformattedDate = jsonObject.getLong("dt")
                        val tempObject = jsonObject.getJSONObject("temp")
                        val temp = tempObject.getDouble("day")
                        val weatherArray = jsonObject.getJSONArray("weather")
                        val jsonObj = weatherArray.getJSONObject(0)
                        val description = jsonObj.getString("description")
                        val iconDay=jsonObj.getString("icon")
                        val sb = StringBuilder()
                        dayData.add(
                            WeatherData(
                                sb.append(Math.floor(temp - 273).toInt()).append("\u2103").toString(),
                                description, formatDate(unformattedDate * 1000),
                                formatTime(unformattedDate * 1000),
                                iconDay
                            )
                        )
                    }
                    mDayAdapter.updateData(dayData,progressDays)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
            }
        )

    // Access the RequestQueue through your singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    private fun formatDate(date: Long): String {
        val simpleDateFormat = SimpleDateFormat("MMM dd,yyyy")
        val formatDate = Date(date)
        return simpleDateFormat.format(formatDate)
    }

    private fun formatTime(date: Long): String {
        val simpleDateFormat = SimpleDateFormat("hh:mm a")
        val formatTime = Date(date)
        return simpleDateFormat.format(formatTime)
    }

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest!!.setPriority(LocationRequest.PRIORITY_LOW_POWER)
        mLocationRequest!!.setInterval(LOCATION_REQUEST_INTERVAL).fastestInterval=
            LOCATION_REQUEST_INTERVAL
        //mLocationRequest.setNumUpdates(3)
        requestLocationUpdate()
    }

    private fun requestLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED ) {
            Log.e("permission", "denied")
            ActivityCompat.requestPermissions(
                this, arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION),
                34
            )
        }

        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }
}
