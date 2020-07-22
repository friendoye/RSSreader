package com.friendoye.rss_reader.utils

import com.github.zsoltk.compose.router.BackStack
import java.lang.IllegalStateException
import kotlin.reflect.KProperty
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaGetter

@Suppress("UNCHECKED_CAST")
val <T> BackStack<T>.elements: List<T>
    get() {
        val elemProperty = BackStack::class.memberProperties.find { it.name == "elements" }
        elemProperty?.javaGetter?.isAccessible = true
        return elemProperty?.javaGetter?.invoke(this) as? List<T>
            ?: throw IllegalStateException("Could not retrieve elements from BackStack.")
    }