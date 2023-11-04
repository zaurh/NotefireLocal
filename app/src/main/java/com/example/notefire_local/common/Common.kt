package com.example.notefire_local.common

import android.os.Parcelable
import android.text.format.DateFormat
import androidx.navigation.NavController


//Convert from currentMillis to Readable Date
fun convertDate(dateInMilliseconds: String, dateFormat: String): String {
    return DateFormat.format(dateFormat, dateInMilliseconds.toLong()).toString()
}

data class NavParam(
    val name: String,
    val value: Parcelable
)

fun navigateTo(navController: NavController, dest: String, vararg params: NavParam) {
    for (param in params) {
        navController.currentBackStackEntry?.savedStateHandle?.set(param.name, param.value)
    }
    navController.navigate(dest) {
        popUpTo(dest)
        launchSingleTop = true
    }
}