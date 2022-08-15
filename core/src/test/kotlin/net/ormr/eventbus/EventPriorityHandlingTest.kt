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

import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe

class EventPriorityHandlingTest : ExpectSpec({
    val events = EventBus.newDefault()

    class EventListener {
        @Subscribed(EventPriority.LOWEST)
        fun `lowest priority level`(event: PriorityEvent) {
            event.value++ shouldBe 0
        }

        @Subscribed(EventPriority.LOW)
        fun `low priority level`(event: PriorityEvent) {
            event.value++ shouldBe 1
        }

        @Subscribed(EventPriority.NORMAL)
        fun `normal priority level`(event: PriorityEvent) {
            event.value++ shouldBe 2
        }

        @Subscribed(EventPriority.HIGH)
        fun `high priority level`(event: PriorityEvent) {
            event.value++ shouldBe 3
        }

        @Subscribed(EventPriority.HIGHEST)
        fun `highest priority level`(event: PriorityEvent) {
            event.value++ shouldBe 4
        }
    }

    context("firing an event to a class <${EventListener::class}> that has multiple event listener functions with different priorities") {
        expect("that they should all fire in the correct order") {
            val listener = EventListener()
            events.subscribe(listener)

            val event = PriorityEvent(value = 0)

            events.fire(event)

            event.value shouldBe 5
        }
    }
})