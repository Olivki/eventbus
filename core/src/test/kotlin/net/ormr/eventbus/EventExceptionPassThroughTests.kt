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

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

class EventExceptionPassThroughTests : ExpectSpec({
    class CustomException : Exception("This is a custom exception.")

    val events = EventBus.newDefault()

    class EventListener {
        @Subscribed
        fun `event handler function that throws a CustomException`(event: EmptyEvent) {
            throw CustomException()
        }
    }

    context("firing an EmptyEvent to an event-listener that has a function that will throw a CustomException") {
        expect("that the exception should be passed up where the 'fire' function is invoked") {
            val listener = EventListener()
            events.subscribe(listener)
            val exception = shouldThrow<CustomException> { events.fire(EmptyEvent()) }
            exception.message shouldBe "This is a custom exception."
        }
    }
})