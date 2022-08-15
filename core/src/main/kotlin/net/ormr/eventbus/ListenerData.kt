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

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandleInfo
import java.lang.invoke.MethodHandles.Lookup
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

public class ListenerData<L : Any, E : Any> internal constructor(
    public val eventBus: EventBus<L, E>,
    public val annotation: Subscribed,
    public val function: KFunction<*>,
    public val handle: MethodHandle,
    public val lookup: Lookup,
    public val handleInfo: MethodHandleInfo,
    public val listener: L,
) : Comparable<ListenerData<*, *>> {
    @Suppress("UNCHECKED_CAST")
    internal val eventClass: Class<E> = (function.valueParameters.first().type.classifier!! as KClass<E>).javaObjectType

    internal val executor: EventExecutor<L, E> = eventBus.factory.create(this)

    @JvmSynthetic
    internal fun fire(event: E) {
        executor.execute(listener, event)
    }

    override fun compareTo(other: ListenerData<*, *>): Int = annotation.priority.compareTo(other.annotation.priority)

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is ListenerData<*, *> -> false
        eventBus != other.eventBus -> false
        annotation != other.annotation -> false
        handleInfo.toString() != other.handleInfo.toString() -> false
        listener != other.listener -> false
        executor != other.executor -> false
        else -> true
    }

    override fun hashCode(): Int {
        var result = eventBus.hashCode()
        result = 31 * result + annotation.hashCode()
        result = 31 * result + handleInfo.toString().hashCode()
        result = 31 * result + listener.hashCode()
        result = 31 * result + executor.hashCode()
        return result
    }
}