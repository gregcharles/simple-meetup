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

// tag::eventRepositoryStructure[]
import org.springframework.data.repository.Repository;

import java.time.LocalDate;
import java.util.Optional;

// end::eventRepositoryStructure[]

/**
 * @author Michael J. Simons, 2017-10-31
 */
// tag::eventRepositoryStructure[]
interface EventRepository extends Repository<Event, Integer> {
    Optional<Event> findOneByHeldOn(LocalDate heldOn);

    Event save(Event newEvent);
}
// end::eventRepositoryStructure[]
