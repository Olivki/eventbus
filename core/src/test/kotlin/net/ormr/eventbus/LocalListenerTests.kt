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
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue

class LocalListenerTests : ExpectSpec({
    val events = EventBus.newDefault()

    context("creating a local object tied to a variable to act as a listener") {
        val listener = object {
            @Subscribed
            fun `subscribed function`(event: EmptyEvent) {
                fail("function invoked")
            }
        }

        events.subscribe(listener)

        expect("that the object will receive invocations") {
            shouldFail { events.fire(EmptyEvent()) }
        }

        expect("that the object can be unregistered") {
            events.isSubscribed(listener).shouldBeTrue()
            events.unsubscribe(listener)
            events.isSubscribed(listener).shouldBeFalse()
        }
    }

    context("creating a local object directly in the register invocation to act as a listener") {
        expect("that the object will receive invocations") {
            events.subscribe(object {
                @Subscribed
                fun `subscribed function`(event: EmptyEvent) {
                    fail("function invoked")
                }
            })
            shouldFail { events.fire(EmptyEvent()) }
        }
    }
})