package com.example.weather.domain

data class Weather(
    val city: String,
    val temp: String,
    val lastDataUpdate: String,
    val iconUrl: String
)
