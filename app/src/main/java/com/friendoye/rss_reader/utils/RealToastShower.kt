package com.friendoye.rss_reader.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.friendoye.rss_reader.ui.ToastShower

class RealToastShower(
    private val context: Context
): ToastShower {

    override fun showShort(@StringRes messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show()
    }

    override fun showLong(@StringRes messageRes: Int) {
        Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show()
    }
}