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
package ac.simons.tdd.app;

import ac.simons.tdd.domain.EventService;
import ac.simons.tdd.domain.NoSuchEventException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michael J. Simons, 2017-10-31
 */
@RestController
@RequestMapping("/api/events")
@SuppressWarnings({"checkstyle:DesignForExtension"})
public class EventsApi {
    private final EventService eventService;

    private final EventResourceAssembler eventResourceAssembler;

    public EventsApi(final EventService eventService, final EventResourceAssembler eventResourceAssembler) {
        this.eventService = eventService;
        this.eventResourceAssembler = eventResourceAssembler;
    }

    @GetMapping
    public Resources<EventResource> events() {
        final List<EventResource> eventResources = eventResourceAssembler.toResources(this.eventService.getOpenEvents());
        return new Resources<EventResource>(eventResources, linkTo(methodOn(this.getClass()).events()).withRel("self"));
    }

    @GetMapping("/{heldOn}/{name}")
    public EventResource event(
        @PathVariable @DateTimeFormat(iso = ISO.DATE)
        final LocalDate heldOn,
        @PathVariable
        final String name
    ) {
        return this.eventService
            .getEvent(heldOn, name)
            .map(eventResourceAssembler::toResource)
            .orElseThrow(NoSuchEventException::new);
    }
 }
