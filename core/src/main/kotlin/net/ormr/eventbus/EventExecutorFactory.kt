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

import net.ormr.eventbus.factories.EventExecutorAsmFactory
import net.ormr.eventbus.factories.EventExecutorMethodHandleFactory
import net.ormr.eventbus.factories.EventExecutorReflectionFactory

public interface EventExecutorFactory {
    public companion object {
        @JvmStatic
        public fun reflection(invokePrivate: Boolean): EventExecutorFactory =
            EventExecutorReflectionFactory(invokePrivate)

        @JvmStatic
        public fun methodHandle(): EventExecutorFactory = EventExecutorMethodHandleFactory

        @JvmStatic
        public fun asm(): EventExecutorFactory = EventExecutorAsmFactory()
    }

    /**
     * A human-readable name of the [EventExecutorFactory].
     */
    public val name: String

    /**
     * Returns whether the executors created by this factory can invoke listener functions with `private` visibility.
     */
    public val canInvokePrivateFunctions: Boolean

    /**
     * Returns a new [EventExecutor] instance that will invoke the method pointed at by the given [data].
     *
     * The manner of how the `EventExecutor` instance is created is implementation specific.
     */
    public fun <L : Any, E : Any> create(data: ListenerData<L, E>): EventExecutor<L, E>
}