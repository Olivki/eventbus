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

package net.ormr.eventbus;

import java.lang.invoke.MethodHandles;

public class Gamer {
    public record MessageEvent(String message) implements Event {
    }

    public static class ListenerBaby {
        @Subscribed
        private void listener(final MessageEvent event) {
            System.out.println(event.message());
        }
    }

    public static void main(final String[] args) {
        var eventBus = EventBus.newDefault(EventExecutorFactory.asm());
        var listener = new ListenerBaby();
        eventBus.subscribe(listener, MethodHandles.lookup());
        eventBus.fire(new MessageEvent("Hello, World!"));

        // eventBus.on(MessageEvent.class, event -> System.out.println(event.message()));
        // eventBus.fire(new MessageEvent("Hello, World!"));
        // System.out.println("Hello?");
    }
}
