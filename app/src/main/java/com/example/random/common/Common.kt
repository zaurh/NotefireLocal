package com.example.random.common

import android.text.format.DateFormat


//Convert from currentMillis to Readable Date
fun convertDate(dateInMilliseconds: String, dateFormat: String): String {
    return DateFormat.format(dateFormat, dateInMilliseconds.toLong()).toString()
}