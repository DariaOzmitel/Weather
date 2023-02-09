package com.example.weather.presentation

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.weather.R

@BindingAdapter("lastUpdateText")
fun bindLastUpdateText(textView: TextView, lastUpdate: String?) {
    if (!lastUpdate.isNullOrEmpty()) {
        textView.text = String.format(
            textView.context.getString(R.string.lastDataUpdate),
            lastUpdate
        )
    } else {
        textView.text = ""
    }
}

@BindingAdapter("tempText")
fun bind(textView: TextView, temp: String?) {

    if (!temp.isNullOrEmpty()) {
        textView.text = String.format(
            textView.context.getString(R.string.tvTemp),
            temp.toFloat().toInt().toString()
        )
    } else {
        textView.text = ""
    }
}