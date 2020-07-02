package com.friendoye.rss_reader.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

class ToastShower(
    private val context: Context
) {

    fun showShort(@StringRes messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }

    fun showLong(@StringRes messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
    }
}