package com.friendoye.rss_reader.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.ui.test.SemanticsNodeInteraction
import androidx.ui.test.runOnUiThread
import java.util.concurrent.atomic.AtomicBoolean

class ComposeMatcherIdlingResource(
    private val nodeChecker: () -> SemanticsNodeInteraction
) : IdlingResource {

    override fun getName(): String = "ComposeMatcherIdlingResource[$nodeChecker]"

    private var isIdleCheckScheduled = false

    private val handler = Handler(Looper.getMainLooper())

    private val isRegistered = AtomicBoolean(false)
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    final override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        resourceCallback = callback
    }

    protected fun transitionToIdle() {
        resourceCallback?.onTransitionToIdle()
    }

    /**
     * Registers this resource into Espresso.
     *
     * Can be called multiple times.
     */
    fun registerSelfIntoEspresso() {
        if (isRegistered.compareAndSet(false, true)) {
            IdlingRegistry.getInstance().register(this)
        }
    }

    /**
     * Unregisters this resource from Espresso.
     *
     * Can be called multiple times.
     */
    fun unregisterSelfFromEspresso() {
        if (isRegistered.compareAndSet(true, false)) {
            IdlingRegistry.getInstance().unregister(this)
        }
    }

    /**
     * Returns whether or not Compose is idle, without starting to poll if it is not.
     */
    fun isIdle(): Boolean {
        return runOnUiThread {
            try {
                println("Here")
                nodeChecker()
                println("Here 2")
                true
            } catch (e: Exception) {
                Log.e("Test", "Exception", e)
                // Do nothing
                false
            }
        }
    }

    /**
     * Returns whether or not Compose is idle, and starts polling if it is not. Will always be
     * called from the main thread by Espresso, and should _only_ be called from Espresso. Use
     * [isIdle] if you need to query the idleness of Compose manually.
     */
    override fun isIdleNow(): Boolean {
        val isIdle = isIdle()
        if (!isIdle) {
            scheduleIdleCheck()
        }
        return isIdle
    }

    private fun scheduleIdleCheck() {
        if (!isIdleCheckScheduled) {
            isIdleCheckScheduled = true
            handler.post {
                isIdleCheckScheduled = false
                if (isIdle()) {
                    transitionToIdle()
                } else {
                    scheduleIdleCheck()
                }
            }
        }
    }
}