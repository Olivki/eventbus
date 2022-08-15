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

private interface ConstrainedListener

class EventListenerTypeTest : ExpectSpec({
    val events = EventBus<ConstrainedListener, Event>()

    class ValidListener : ConstrainedListener {
        @Subscribed
        fun `i should be valid`(event: EmptyEvent) {
            fail("i've been invoked")
        }
    }

    context("registering a listener that inherits from the specified listener super-class") {
        expect("that the listener should be properly registered") {
            events.subscribe(ValidListener())

            shouldFail { events.fire(EmptyEvent()) }
        }
    }
})