/*
 * Copyright 2016 Open mHealth
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

package org.openmhealth.dsu.service;

import org.openmhealth.schema.domain.omh.DataPoint;

import javax.annotation.Nullable;


/**
 * A service that searches for data points.
 *
 * @author Emerson Farrugia
 */
public interface DataPointSearchService {

    /**
     * @param queryFilter RSQL-formatted query to parse and pass along to mongo
     * @param offset the index of the first matching data point to return
     * @param limit the number of matching data points to return
     * @return the result of the search
     */
    Iterable<DataPoint> findBySearchCriteria(String queryFilter, @Nullable Integer offset,
                                             @Nullable Integer limit);
}
