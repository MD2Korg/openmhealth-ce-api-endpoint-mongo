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

package org.openmhealth.dsu.controller;

import org.openmhealth.dsu.service.DataPointSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static org.openmhealth.dsu.controller.DataPointSearchController.FILTER_PARAMETER;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;

/**
 * A controller that finds and retrieves participant id from datapoints.
 *
 * @author Wallace Wadge
 */
@Controller
public class ParticipantController {

    @Autowired
    private DataPointSearchService dataPointSearchService;

    /**
     * Finds and retrieves data point participants
     * @param queryFilter a filter to limit results
     * @return a list of matching data points
     */
    @RequestMapping(value = "/v1.0.M2/participants", method = {HEAD, GET}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Iterable<String>> findDataPointParticipants(@RequestParam(value = FILTER_PARAMETER) final List<String> queryFilter) {

        Iterable<String> dataPoints = dataPointSearchService.findParticipantsBySearchCriteria(queryFilter);

        return new ResponseEntity<>(dataPoints, new HttpHeaders(), OK);
    }

}
