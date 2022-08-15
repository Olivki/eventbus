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
import io.kotest.matchers.shouldBe

class EventLambdaTest : ExpectSpec({
    val events = EventBus.newDefault()

    context("firing an EmptyEvent to an event-listener lambda") {
        expect("that the listener lambda should be invoked") {
            events.on<EmptyEvent> { fail("it was invoked!") }
            shouldFail { events.fire(EmptyEvent()) }
        }
    }

    context("firing a MessageEvent to an event-listener lambda") {
        expect("that the listener lambda should be invoked with the correct instance") {
            events.on<MessageEvent> { fail(message) }
            val error = shouldThrow<AssertionError> { events.fire(MessageEvent("Hello, World!")) }
            error.message shouldBe "Hello, World!"
        }
    }

    context("firing an EmptyEvent to an event-listener lambda via fireSuspended") {
        expect("that the listener lambda should be invoked with") {
            events.on<EmptyEvent> { fail("it was invoked!") }
            shouldFail { events.fireSuspended(EmptyEvent()) }
        }
    }
})