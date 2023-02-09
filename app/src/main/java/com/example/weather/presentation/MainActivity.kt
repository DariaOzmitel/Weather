package com.example.weather.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
import com.squareup.picasso.Picasso
import com.yariksoffice.lingver.Lingver
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var fLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: MainViewModel

    private var serverId = ID_SERVER2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.title_Main)
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

    private fun setLanguage(language: String) {
        Lingver.getInstance().setLocale(this, language)
        recreate()
    }

    private fun isLocationEnabled(): Boolean {
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
            if (it.result != null) {

                val url = getUrl(it.result.latitude.toString(), it.result.longitude.toString())
                getWeatherData(url)
            } else {
                Toast.makeText(this, getString(R.string.locationNotFound), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.server1 -> {
                serverId = ID_SERVER1
                getLocation()
            }
            R.id.server2 -> {
                serverId = ID_SERVER2
                getLocation()
            }
        }
        return true
    }

    private fun getUrl(lat: String, lon: String): String {
        if (serverId == ID_SERVER1) {
            return "https://api.weatherapi.com/v1/current.json" +
                    "?key=$API_KEY" +
                    "&q=$lat,$lon" +
                    "&aqi=no"
        }
        if (serverId == ID_SERVER2) {
            return "https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$lon&" +
                    "appid=$API_KEY2"
        }
        return ""
    }

    private fun getWeatherData(url: String) {
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                parseWeatherData(JSONObject(response))
            },
            {
                Log.d("MyLog", "Volley error: $it")
            }
        )
        queue.add(stringRequest)
    }

    private fun parseWeatherData(jsonObject: JSONObject) {

        if (serverId == ID_SERVER1) {
            val weather = Weather(
                jsonObject.getJSONObject("location").getString("name"),
                jsonObject.getJSONObject("current").getString("temp_c"),
                jsonObject.getJSONObject("current").getString("last_updated"),
                "https:" + jsonObject.getJSONObject("current")
                    .getJSONObject("condition").getString("icon")
            )
            viewModel.updateWeatherData(weather)
            Picasso.get().load(viewModel.weather.value?.iconUrl).into(binding.imageView)
        }

        if (serverId == ID_SERVER2) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd' 'HH:mm")
            val date = java.util.Date(jsonObject.getString("dt").toLong() * 1000)

            val weatherArrow = jsonObject.getJSONArray("weather")[0] as JSONObject
            val icon = weatherArrow.getString("icon")
            val weather = Weather(
                jsonObject.getString("name"),
                ((jsonObject.getJSONObject("main")
                    .getString("temp").toFloat()) - 273.15).toInt().toString(),
                sdf.format(date),
                " https://openweathermap.org/img/wn/" +
                        "$icon@2x.png"
            )

            viewModel.updateWeatherData(weather)

            when (icon.substring(0, 2).toInt()) {
                1 -> binding.imageView.setImageResource(R.drawable.sunny)
                in 2..4 -> binding.imageView.setImageResource(R.drawable.cloudy)
                in 9..11 -> binding.imageView.setImageResource(R.drawable.rain)
                else -> binding.imageView.setImageResource(R.drawable.cloudy)
            }
        }

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
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
        }
    }

    companion object {
        private const val API_KEY = "b0446bc0f616488096c175435230702"
        private const val API_KEY2 = "ae38e6faecf8d70b5eacffea9169bb37"
        private const val ID_SERVER1 = 1
        private const val ID_SERVER2 = 2
    }
}
