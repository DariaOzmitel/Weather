package com.example.weather.presentation

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.weather.R
import com.squareup.picasso.Picasso

@BindingAdapter("lastUpdateText")
fun bindLastUpdateText(textView: TextView, lastUpdate: String?) {
    if (!lastUpdate.isNullOrEmpty()){
        textView.text = String.format(
            textView.context.getString(R.string.lastDataUpdate),
            lastUpdate
        )
    }
    else {
        textView.text = String.format(
            textView.context.getString(R.string.lastDataUpdate),
            ""
        )
    }
}

@BindingAdapter("loadImage")
fun bindLoadImage(imageView: ImageView, imageUrl: String?) {
    Picasso.get().load("https:$imageUrl").into(imageView)
}

@BindingAdapter("tempText")
fun bind(textView: TextView, temp: String?) {

    if (!temp.isNullOrEmpty()) {
        textView.text = String.format(
            textView.context.getString(R.string.tvTemp),
            temp.toFloat().toInt().toString()
        )
    } else {
        textView.text = String.format(
            textView.context.getString(R.string.tvTemp),
            ""
        )
    }
}