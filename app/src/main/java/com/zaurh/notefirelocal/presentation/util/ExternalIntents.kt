package com.zaurh.notefirelocal.presentation.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri

private const val DEVELOPER_EMAIL = "zaurway@gmail.com"

/** Opens this app's page in the Play Store app, or in the browser if Play isn't installed. */
fun Context.openPlayStorePage() {
    val market = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
    val web = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri())
    startFirst(market, web)
}

fun Context.emailDeveloper(subject: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        putExtra(Intent.EXTRA_EMAIL, arrayOf(DEVELOPER_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    startFirst(intent)
}

fun Context.shareText(text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startFirst(Intent.createChooser(send, chooserTitle))
}

private fun Context.startFirst(vararg intents: Intent) {
    for (intent in intents) {
        try {
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            return
        } catch (_: ActivityNotFoundException) {
            // Try the next option.
        }
    }
}
