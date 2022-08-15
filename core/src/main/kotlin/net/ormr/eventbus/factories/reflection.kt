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

package net.ormr.eventbus.factories

import net.ormr.eventbus.EventExecutor
import net.ormr.eventbus.EventExecutorFactory
import net.ormr.eventbus.ListenerData
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap

internal class EventExecutorReflectionFactory(invokePrivate: Boolean) : EventExecutorFactory {
    override val name: String
        get() = "reflection"
    override val canInvokePrivateFunctions: Boolean = invokePrivate
    private val methodCache = ConcurrentHashMap<String, Method>()

    override fun <L : Any, E : Any> create(data: ListenerData<L, E>): EventExecutor<L, E> {
        val method = methodCache.computeIfAbsent(data.handleInfo.toString()) {
            data.handleInfo.reflectAs(Method::class.java, data.lookup)
        }

        // TODO: do we want to do this in the cache above? idk if accessible saves
        if (canInvokePrivateFunctions) {
            method.isAccessible = true
        }

        return object : EventExecutor<L, E> {
            override fun execute(listener: L, event: E) {
                try {
                    method.invoke(listener, event)
                } catch (e: InvocationTargetException) {
                    // unwrap exception and rethrow it
                    throw e.targetException
                }
            }
        }
    }
}