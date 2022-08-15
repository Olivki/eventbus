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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class UnregisterEventListenerTest : ExpectSpec({
    val events = EventBus.newDefault()

    class EventListener {
        @Subscribed
        fun `unregister event listener test`(event: EmptyEvent) {
            fail("event listener was not unregistered")
        }
    }

    context("attempting to unregister a class <${EventListener::class}> that's registered as an event-listener") {
        expect("that the event listener function should not be called") {
            val listener = EventListener()

            events.subscribe(listener)
            events.isSubscribed(listener).shouldBeTrue()
            shouldFail { events.fire(EmptyEvent()) }
            events.unsubscribe(listener).shouldBeTrue()
            events.isSubscribed(listener).shouldBeFalse()
            events.fire(EmptyEvent())
        }
    }
})