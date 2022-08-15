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

internal object EventExecutorMethodHandleFactory : EventExecutorFactory {
    override val name: String
        get() = "method handles"

    override val canInvokePrivateFunctions: Boolean get() = true

    override fun <L : Any, E : Any> create(data: ListenerData<L, E>): EventExecutor<L, E> =
        object : EventExecutor<L, E> {
            override fun execute(listener: L, event: E) {
                data.handle.invokeWithArguments(listener, event)
            }
        }
}