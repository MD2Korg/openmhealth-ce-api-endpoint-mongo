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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import static java.lang.String.format;
import static org.openmhealth.dsu.configuration.OAuth2Properties.CLIENT_ROLE;
import static org.openmhealth.dsu.configuration.OAuth2Properties.DATA_POINT_READ_SCOPE;
import static org.openmhealth.dsu.controller.CommonControllerUtil.getEndUserId;
import static org.openmhealth.dsu.controller.DataPointSearchController.*;
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
     * Finds and retrieves data points returning the participant ID.
     *
     * @param offset the number of data points to skip
     * @param limit  the number of data points to return
     * @return a list of matching data points
     */
    // TODO confirm if HEAD handling needs anything additional
    // only allow clients with read scope to read data points
    @PreAuthorize("#oauth2.clientHasRole('" + CLIENT_ROLE + "') and #oauth2.hasScope('" + DATA_POINT_READ_SCOPE + "')")
    // TODO look into any meaningful @PostAuthorize filtering
    @RequestMapping(value = "/v1.0.M2/dataPointsByParticipant", method = {HEAD, GET}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Iterable<String>> findDataPoints(
            @RequestParam(value = FILTER_PARAMETER) final String queryFilter,
            @RequestParam(value = RESULT_OFFSET_PARAMETER, defaultValue = "0") final Integer offset,
            @RequestParam(value = RESULT_LIMIT_PARAMETER, defaultValue = DEFAULT_RESULT_LIMIT) final Integer limit,
            Authentication authentication) {

        String filter = queryFilter;

        // determine the user associated with the access token to restrict the search accordingly
        String endUserId = getEndUserId(authentication, null);
        if (endUserId != null) {
            filter = format("header.user_id == '%s' and %s", endUserId, queryFilter);
        }

        Iterable<String> dataPoints = dataPointSearchService.findParticipantsBySearchCriteria(filter, offset, limit);

        return new ResponseEntity<>(dataPoints, new HttpHeaders(), OK);
    }

}
