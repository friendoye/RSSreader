package com.friendoye.rss_reader.ui

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

interface ToastShower {
    fun showShort(@StringRes messageRes: Int)
    fun showLong(@StringRes messageRes: Int)
}