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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.io.Serializable;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

/**
 * @author Michael J. Simons, 2017-10-31
 */
@Entity
@Table(
    name = "events",
    uniqueConstraints = {
        @UniqueConstraint(name = "events_uk", columnNames = {"held_on", "name"})
    }
)
@JsonAutoDetect(fieldVisibility = NONE, getterVisibility = NONE, isGetterVisibility = NONE)
@Getter
@SuppressWarnings({"checkstyle:DesignForExtension"})
// tag::eventStructure[]
public class Event implements Serializable {

    public enum Status {

        open, closed
    }

    // end::eventStructure[]
    static final ThreadLocal<Clock> CLOCK =
        ThreadLocal.withInitial(() -> Clock.systemDefaultZone()); // <1>

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "held_on", nullable = false)
    @JsonProperty
    // tag::eventStructure[]
    private LocalDate heldOn;

    // end::eventStructure[]
    @Column(length = 512, nullable = false)
    @JsonProperty
    // tag::eventStructure[]
    private String name;

    // end::eventStructure[]
    @Column(name = "number_of_seats", nullable = false)
    // tag::eventStructure[]
    private Integer numberOfSeats;

    // end::eventStructure[]
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // tag::eventStructure[]
    private Status status;

    // end::eventStructure[]
    @ElementCollection
    @CollectionTable(name = "registrations", joinColumns = @JoinColumn(name = "event_id"))
    // tag::eventStructure[]
    private List<Registration> registrations = new ArrayList<>();

    // end::eventStructure[]
    Event() {
    }

    @JsonCreator
    public Event(@JsonProperty("heldOn") final LocalDate heldOn, @JsonProperty("name") final String name) {
        this(heldOn, name, 20);
    }

    // tag::eventStructure[]
    public Event(final LocalDate heldOn, final String name, final Integer numberOfSeats) { // <1>
        if (heldOn == null || heldOn.isBefore(LocalDate.now(CLOCK.get()))) {
            throw new IllegalArgumentException("Event requires a date in the future.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Event requires a non-empty name.");
        }

        this.heldOn = heldOn;
        this.name = name;
        this.status = Status.open;

        this.setNumberOfSeats(numberOfSeats);
    }

    // end::eventStructure[]

    public Integer getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(final Integer numberOfSeats) {
        if (numberOfSeats == null || numberOfSeats < 0) {
            throw new IllegalArgumentException("Event requires some seats.");
        }

        this.numberOfSeats = numberOfSeats;
    }

    public List<Registration> getRegistrations() {
        return Collections.unmodifiableList(this.registrations);
    }

    public boolean isPastEvent() {
        return this.heldOn.isBefore(LocalDate.now(CLOCK.get()));
    }

    public boolean isOpen() {
        return this.status == Status.open;
    }

    public boolean isClosed() {
        return this.status == Status.closed;
    }

    public boolean isFull() {
        return this.registrations.size() == this.numberOfSeats;
    }

    public void close() {
        this.status = Status.closed;
    }

    @JsonProperty
    public Integer getNumberOfFreeSeats() {
        return this.numberOfSeats - this.registrations.size();
    }

    // tag::eventStructure[]
    public Registration register(final Person person) { // <2>
        // end::eventStructure[]
        if (isClosed()) {
            throw new IllegalStateException("Cannot register for a closed event.");
        }
        // tag::eventStructure[]
        if (isPastEvent()) {
            throw new IllegalStateException("Cannot register for a past event.");
        }
        if (isFull()) {
            throw new IllegalStateException("Cannot register for a full event.");
        }

        // Weitere Bedingungen ausgeblendet
        // end::eventStructure[]
        final Registration registration = new Registration(person);
        if (this.registrations.contains(registration)) {
            throw new IllegalArgumentException("Already registered with email-addess" + person.getEmail());
        }
        // tag::eventStructure[]
        this.registrations.add(registration);
        return registration;
    }
    // end::eventStructure[]

    Example<Event> asExample() {
        return Example.of(this, ExampleMatcher.matching()
            .withIgnoreNullValues()
            .withIgnorePaths("numberOfSeats", "status")
            .withMatcher("heldOn", match -> match.exact())
            .withMatcher("name", match -> match.exact())
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Event event = (Event) o;
        return Objects.equals(heldOn, event.heldOn) && Objects.equals(name, event.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(heldOn, name);
    }

// tag::eventStructure[]
}
// end::eventStructure[]
