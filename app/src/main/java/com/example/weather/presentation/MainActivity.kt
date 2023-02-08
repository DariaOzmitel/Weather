package com.example.weather.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weather.*
import com.example.weather.databinding.ActivityMainBinding
import com.example.weather.domain.Weather
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.yariksoffice.lingver.Lingver
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkPermission()

    }

    private fun init() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        fLocationClient = LocationServices.getFusedLocationProviderClient(this)
        binding.btRefresh.setOnClickListener {
            getLocation()
        }
        binding.ibtRu.setOnClickListener {
            setLanguage("ru")
        }
        binding.ibtEn.setOnClickListener {
            setLanguage("en")
        }
    }

    private fun setLanguage(language: String){
        Lingver.getInstance().setLocale(this,language)
        recreate()
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        if (!isLocationEnabled()) {
            Toast.makeText(this, getString(R.string.locationError), Toast.LENGTH_LONG).show()
            return
        }
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            ct.token
        ).addOnCompleteListener {
            if(it.result != null)
                getWeatherData("${it.result.latitude},${it.result.longitude}")
            else {
                Toast.makeText(this, getString(R.string.locationNotFound), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun getWeatherData(coordinates: String) {
        val url = "https://api.weatherapi.com/v1/current.json" +
                "?key=$API_KEY" +
                "&q=$coordinates" +
                "&aqi=no"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                parseWeatherData(JSONObject(response))
                Log.d("MyLog", "Response: $response")
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }
        )
        queue.add(stringRequest)
    }

    private fun parseWeatherData(jsonObject: JSONObject) {

        val weather = Weather(
            jsonObject.getJSONObject("location").getString("name"),
            jsonObject.getJSONObject("current").getString("temp_c"),
            jsonObject.getJSONObject("current").getString("last_updated"),
            jsonObject.getJSONObject("current").getJSONObject("condition").getString("icon")
        )
        viewModel.updateWeatherData(weather)
        Log.d("MyLog", "Response: $weather")
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission() {
        if (!isPermissionGranted()) {
            permissionListener()
            pLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getLocation()
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                getLocation()
            } else {
                Toast.makeText(this, getString(R.string.locationWarning), Toast.LENGTH_LONG).show()
            }
            Log.d("MyLog", "Permission: $it")
        }
    }
    companion object {
        private const val API_KEY = "b0446bc0f616488096c175435230702"
        private const val API_KEY2 = "ae38e6faecf8d70b5eacffea9169bb37"
    }
}
