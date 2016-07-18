/*
 * Copyright 2014 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.dsu.repository;

import org.openmhealth.schema.domain.omh.DataPoint;

import javax.annotation.Nullable;


/**
 * A set of data point search repository methods not automatically implemented by Spring Data repositories.
 *
 * @author Emerson Farrugia
 */
public interface DataPointSearchRepositoryCustom {


    Iterable<DataPoint> findBySearchCriteria(String queryFilter, @Nullable Integer offset,
                                             @Nullable Integer limit);


    Iterable<String> findParticipantsBySearchCriteria(String queryFilter, Integer offset, Integer limit);

}
