/*
 * Copyright 2022 Oliver Berg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ormr.eventbus

import java.util.concurrent.ConcurrentHashMap

internal class ListenerRepository<L : Any, E : Any> {
    private val lock = object {}
    private val listeners: MutableSet<ListenerData<L, E>> = ConcurrentHashMap.newKeySet()
    private val cachedListeners: MutableList<ListenerData<L, E>> = mutableListOf()
    private var isCacheInvalidated: Boolean = false

    private fun checkCachedListeners() {
        if (isCacheInvalidated) {
            synchronized(lock) {
                cachedListeners.apply {
                    clear()
                    // sort the listeners by their priority
                    addAll(listeners.sorted())
                }
                isCacheInvalidated = false
            }
        }
    }

    fun notifyAll(event: E) {
        checkCachedListeners()
        cachedListeners.forEach { it.executor.execute(it.listener, event) }
    }

    fun subscribe(listener: ListenerData<L, E>) {
        listeners.add(listener)
        isCacheInvalidated = true
    }

    fun unsubscribe(listener: ListenerData<L, E>) {
        listeners.remove(listener)
        isCacheInvalidated = true
    }

    fun unsubscribeIf(predicate: (ListenerData<L, E>) -> Boolean) {
        listeners.removeIf(predicate)
        if (listeners.size != cachedListeners.size) isCacheInvalidated = true
    }

    fun isSubscribed(listener: L): Boolean = listeners.any { listener == it.listener }

    override fun toString(): String =
        "ListenerRepository for ${cachedListeners.joinToString { it.eventClass.toString() }}"
}