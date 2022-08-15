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

import kotlinx.coroutines.runBlocking
import net.ormr.eventbus.EventExecutorFactory.Companion.asm
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodHandles.Lookup
import java.util.function.Consumer
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KVisibility.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.typeOf

/**
 * An implementation of the [publish-subscribe pattern](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern).
 *
 * @property listenerClass The super-class of all the listeners registered to `this` bus.
 * @property eventClass The super-class of the events that `this` bus sends.
 * @property factory The [EventExecutorFactory] used by this `bus` to invoke event listener functions.
 */
public class EventBus<L : Any, E : Any> @JvmOverloads constructor(
    public val listenerClass: Class<L>,
    public val eventClass: Class<E>,
    public val factory: EventExecutorFactory = asm(),
) {
    public companion object {
        private val UNIT_TYPE = typeOf<Unit>()

        @JvmStatic
        @JvmOverloads
        public fun newDefault(factory: EventExecutorFactory = asm()): EventBus<Any, Event> = EventBus(factory)
    }

    internal val repositories = hashMapOf<Class<out E>, ListenerRepository<L, E>>()
    private val listeners = hashMapOf<Class<out E>, MutableList<suspend E.() -> Unit>>()

    /**
     * Registers [listener] as an event listener of this bus.
     */
    public fun subscribe(listener: L, lookup: Lookup) {
        require(listenerClass.isInstance(listener)) { "'listener' must be sub-class of $listenerClass" }
        val instanceClass = listener::class
        // TODO: notify the user about faulty listener functions in some manner
        instanceClass.declaredFunctions
            .asSequence()
            .filter { it.extensionReceiverParameter == null }
            .filter {
                if (!it.hasAllowedVisibility()) {
                    throw IllegalArgumentException("The ${factory.name} factory can't create dispatchers for function $it as it has disallowed visibility (${it.visibility}).")
                }

                true
            }
            .filter { it.valueParameters.size == 1 }
            .filter { it.returnType == UNIT_TYPE }
            .filterNot { it.isSuspend } // TODO: we should allow this at some point
            .filter { it.hasAnnotation<Subscribed>() }
            .map { it to (it.javaMethod ?: error("Listener function $it can't be converted to Java Method")) }
            .map { (func, method) -> func to lookup.unreflect(method) }
            .map { (func, handle) ->
                ListenerData(
                    this,
                    func.findAnnotation()!!,
                    func,
                    handle,
                    lookup,
                    lookup.revealDirect(handle),
                    listener,
                )
            }
            .forEach {
                repositories.getOrPut(it.eventClass) { ListenerRepository() }.subscribe(it)
            }
    }

    @JvmSynthetic
    @Suppress("NOTHING_TO_INLINE")
    public inline fun subscribe(listener: L) {
        subscribe(listener, MethodHandles.lookup())
    }

    private fun KFunction<*>.hasAllowedVisibility(): Boolean = when {
        factory.canInvokePrivateFunctions -> visibility == PUBLIC || visibility == INTERNAL || visibility == PRIVATE
        else -> visibility == PUBLIC || visibility == INTERNAL
    }

    public fun unsubscribe(listener: L): Boolean {
        require(listenerClass.isInstance(listener)) { "'listener' must be sub-class of $listenerClass" }

        if (isNotSubscribed(listener)) return false

        for (func in listener::class.declaredFunctions) {
            if (!func.hasAnnotation<Subscribed>()) continue
            val parameters = func.valueParameters.ifEmpty { null } ?: continue
            val eventClass = parameters.first().type.classifier as? KClass<*> ?: continue
            repositories[eventClass.java]?.unsubscribeIf { it.listener == listener }
        }

        return true
    }

    @JvmSynthetic
    @JvmName("suspendOn")
    @Suppress("UNCHECKED_CAST")
    public fun <R : E> on(eventClass: Class<out R>, listener: suspend R.() -> Unit) {
        listeners.getOrPut(eventClass) { mutableListOf() }.add(listener as suspend E.() -> Unit)
    }

    @JvmName("on")
    @Suppress("FunctionName")
    public fun <R : E> `$$java wrapped on$$`(eventClass: Class<out R>, listener: Consumer<R>) {
        on(eventClass) { listener.accept(this) }
    }

    @JvmSynthetic
    public inline fun <reified R : E> on(noinline listener: suspend R.() -> Unit) {
        on(R::class.java, listener)
    }

    public fun <R : E> fire(event: R): R {
        require(eventClass.isInstance(event)) { "'event' must be sub-class of $eventClass" }
        repositories[event::class.java]?.notifyAll(event)
        runBlocking { listeners[event::class.java]?.forEach { it(event) } }
        return event
    }

    // TODO: better name
    @JvmSynthetic
    public suspend fun <R : E> fireSuspended(event: R): R {
        require(eventClass.isInstance(event)) { "'event' must be sub-class of $eventClass" }
        repositories[event::class.java]?.notifyAll(event)
        listeners[event::class.java]?.forEach { it(event) }
        return event
    }

    public fun isSubscribed(listener: L): Boolean = repositories.values.any { it.isSubscribed(listener) }

    public fun isNotSubscribed(listener: L): Boolean = !isSubscribed(listener)
}

@JvmSynthetic
@Suppress("FunctionName")
public inline fun <reified L : Any, reified E : Any> EventBus(
    factory: EventExecutorFactory = asm(),
): EventBus<L, E> = EventBus(L::class.java, E::class.java, factory)