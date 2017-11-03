/*
 * Copyright 2017 michael-simons.eu.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ac.simons.tdd.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static ac.simons.tdd.domain.Events.alreadyRegisteredEvent;
import static ac.simons.tdd.domain.Events.closedEvent;
import static ac.simons.tdd.domain.Events.fullEvent;
import static ac.simons.tdd.domain.Events.pastEvent;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * @author Michael J. Simons, 2017-10-31
 */
// tag::eventStructureTest[]
@TestInstance(Lifecycle.PER_CLASS) // <1>
class EventTest {
    @BeforeAll // <2>
    public void prepareEventClock() {
        Event.CLOCK.set(
            Clock.fixed(Instant.parse("2018-01-01T08:00:00.00Z"), ZoneId.systemDefault()));
    }

    @Nested // <3>
    class Preconditions {
        @TestFactory // <4>
        Stream<DynamicTest> constructor() {
            return Stream.concat(Stream.of(LocalDate.of(2017, 10, 31), null) // <5>
                .map(date -> dynamicTest(
                    "Constructor should not allow invalid events", () ->
                        assertEquals("Event requires a date in the future.",
                            assertThrows(
                                IllegalArgumentException.class,
                                () -> new Event(date, "test")
                            ).getMessage()))
                ), Stream.of(null, "", "\t", " ")
                  .map(name -> dynamicTest(
                        "Constructor should not allow invalid events", () ->
                              assertEquals("Event requires a non-empty name.",
                                    assertThrows(
                                          IllegalArgumentException.class,
                                          () -> new Event(LocalDate.of(2018, 1, 2), name)
                                    ).getMessage()))
                  ));

        }
    }

    @Nested
    class Postconditions { // <6>
        @Test
        @DisplayName("Constructor should create valid events")
        void constructor() {
            final Integer numberOfSeats = 23;
            final LocalDate heldOn = LocalDate.of(2018, 1, 2);
            final Event event = new Event(
                heldOn, "test", numberOfSeats);
            assertAll(
                () -> assertEquals(heldOn, event.getHeldOn()),
                () -> assertEquals("test", event.getName()),
                () -> assertEquals(numberOfSeats, event.getNumberOfSeats()),
                () -> assertTrue(event.isOpen())
            ); // <7>
        }
    }

    @TestFactory
    @DisplayName("Registration logic should work")
    Stream<DynamicTest> registrationShouldWork() {  // <8>
        final Map<Event, Class<? extends Exception>> events = new HashMap<>();
        events.put(closedEvent(), IllegalStateException.class);
        events.put(pastEvent(), IllegalStateException.class);
        // end::eventStructureTest[]
        events.put(fullEvent(), IllegalStateException.class);
        events.put(alreadyRegisteredEvent(), IllegalArgumentException.class);
        // tag::eventStructureTest[]

        return events.entrySet().stream().map(entry ->
            dynamicTest("Should not be able to register to event with wrong state", () ->
                assertThrows(
                    entry.getValue(),
                    () -> entry.getKey().registerFor(new Registration("test@test.com", "test"))
                )
            ));
    }
    // tag::eventStructureTest[]
}
// end::eventStructureTest[]
