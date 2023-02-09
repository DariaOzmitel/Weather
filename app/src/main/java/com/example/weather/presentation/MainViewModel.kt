package com.example.weather.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.weather.domain.Weather

class MainViewModel : ViewModel() {

    private val _weather = MutableLiveData<Weather>()
    val weather: LiveData<Weather>
        get() = _weather

    fun updateWeatherData(weather: Weather) {
        _weather.value = weather
    }
}