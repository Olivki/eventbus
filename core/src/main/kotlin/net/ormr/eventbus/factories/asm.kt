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

import net.ormr.asmkt.BytecodeVersion
import net.ormr.asmkt.Modifiers.BRIDGE
import net.ormr.asmkt.Modifiers.PUBLIC
import net.ormr.asmkt.Modifiers.PUBLIC_FINAL_SYNTHETIC
import net.ormr.asmkt.Modifiers.PUBLIC_SYNTHETIC
import net.ormr.asmkt.defineClass
import net.ormr.asmkt.defineMethod
import net.ormr.asmkt.loadThis
import net.ormr.asmkt.types.MethodType
import net.ormr.asmkt.types.MethodType.Companion.createGeneric
import net.ormr.asmkt.types.PrimitiveType.Void
import net.ormr.asmkt.types.ReferenceType
import net.ormr.asmkt.types.ReferenceType.Companion.OBJECT
import net.ormr.eventbus.EventExecutor
import net.ormr.eventbus.EventExecutorFactory
import net.ormr.eventbus.ListenerData
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

internal class EventExecutorAsmFactory : EventExecutorFactory {
    private companion object {
        private val counter = AtomicLong()
        private val eventExecutorType = ReferenceType<EventExecutor<*, *>>()

        fun getCount(): Long = counter.getAndIncrement()
    }

    override val name: String
        get() = "asm"

    override val canInvokePrivateFunctions: Boolean get() = false
    private val cache = ConcurrentHashMap<String, Class<out EventExecutor<*, *>>>()

    @Suppress("UNCHECKED_CAST")
    override fun <L : Any, E : Any> create(data: ListenerData<L, E>): EventExecutor<L, E> {
        val executor = cache.computeIfAbsent(data.handleInfo.toString()) { createExecutor(data) }
        return executor.getDeclaredConstructor().newInstance() as EventExecutor<L, E>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <L : Any, E : Any> createExecutor(data: ListenerData<L, E>): Class<out EventExecutor<L, E>> {
        val listenerClass = data.listener.javaClass
        val count = getCount()
        val type = ReferenceType.fromInternal("${listenerClass.packageName.replace('.', '/')}/event-executor-$count")
        val bytes = createClass(type, data)
        return data.lookup.defineClass(bytes) as Class<out EventExecutor<L, E>>
    }

    private fun createClass(type: ReferenceType, data: ListenerData<*, *>): ByteArray {
        val listener = ReferenceType.of(data.listener.javaClass)
        val event = ReferenceType.of(data.eventClass)
        val listenerMethod = MethodType.of(data.handleInfo.reflectAs(Method::class.java, data.lookup))
        val executeType = MethodType.ofVoid(listener, event)
        return defineClass(
            type,
            BytecodeVersion.JAVA_17,
            access = PUBLIC_FINAL_SYNTHETIC,
            interfaces = listOf(eventExecutorType),
        ) {
            defineDefaultConstructor()

            defineMethod("execute", PUBLIC, executeType) {
                loadLocal(1, listener)
                loadLocal(2, event)
                invokeVirtual(listener, data.handleInfo.name, listenerMethod)
                returnValue()
            }

            // bridge
            defineMethod("execute", PUBLIC_SYNTHETIC or BRIDGE, createGeneric(2, returnType = Void)) {
                loadThis()
                loadLocal(1, OBJECT)
                checkCast(listener)
                loadLocal(2, OBJECT)
                checkCast(event)
                invokeLocalVirtual("execute", executeType)
                returnValue()
            }
        }.toByteArray()
    }
}