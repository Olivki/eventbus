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

import io.kotest.assertions.fail
import io.kotest.assertions.shouldFail
import io.kotest.core.spec.style.ExpectSpec
import net.ormr.eventbus.EventExecutorFactory.Companion.asm
import net.ormr.eventbus.EventExecutorFactory.Companion.methodHandle
import net.ormr.eventbus.EventExecutorFactory.Companion.reflection

class EventExecutorFactoryTest : ExpectSpec({
    for (strategy in listOf(asm(), methodHandle(), reflection(true), reflection(false))) {
        context("creating an event-bus using the '${strategy::class.simpleName!!}' invocation strategy") {
            expect("that it should be able to invoke functions") {
                val events = EventBus.newDefault(strategy)

                class EventListener {
                    @Subscribed
                    fun `receive event func`(event: EmptyEvent) {
                        fail("event was received")
                    }
                }

                val listener = EventListener()

                events.subscribe(listener)

                shouldFail { events.fire(EmptyEvent()) }
            }
        }
    }
})