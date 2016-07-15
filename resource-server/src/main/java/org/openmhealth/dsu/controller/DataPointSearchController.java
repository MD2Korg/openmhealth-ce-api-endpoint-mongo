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

import org.openmhealth.dsu.domain.DataPointSearchCriteria;
import org.openmhealth.dsu.domain.EndUserUserDetails;
import org.openmhealth.dsu.service.DataPointSearchService;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Validator;
import java.time.OffsetDateTime;

import static org.openmhealth.dsu.configuration.OAuth2Properties.CLIENT_ROLE;
import static org.openmhealth.dsu.configuration.OAuth2Properties.DATA_POINT_READ_SCOPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.badRequest;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;


/**
 * A controller that finds and retrieves data points.
 *
 * @author Emerson Farrugia
 */
@Controller
public class DataPointSearchController {

    /*
     * These filtering parameters are temporary. They will likely change when a more generic filtering approach is
     * implemented.
     */
    public static final String CREATED_BEFORE_PARAMETER = "created_before";
    public static final String CREATED_ON_OR_AFTER_PARAMETER = "created_on_or_after";
    public static final String END_USER_ID_PARAMETER = "end_user_id";
    public static final String QUERY_PARAMETER = "query";
    public static final String SCHEMA_NAME_PARAMETER = "schema_name";
    public static final String SCHEMA_NAMESPACE_PARAMETER = "schema_namespace";
    // TODO searching by schema version should support wildcards, sticking to exact match for now
    public static final String SCHEMA_VERSION_PARAMETER = "schema_version";

    public static final String RESULT_OFFSET_PARAMETER = "skip";
    public static final String RESULT_LIMIT_PARAMETER = "limit";
    public static final String DEFAULT_RESULT_LIMIT = "100";


    @Autowired
    private DataPointSearchService dataPointSearchService;

    @Autowired
    private Validator validator;


    /**
     * Finds and retrieves data points.
     *
     * @param schemaNamespace the namespace of the schema the data points conform to
     * @param schemaName the name of the schema the data points conform to
     * @param schemaVersion the version of the schema the data points conform to
     * @param queryFilter an optional RSQL-formatted query to filter results with
     * @param createdOnOrAfter the earliest creation timestamp of the data points to return, inclusive
     * @param createdBefore the latest creation timestamp of the data points to return, exclusive
     * @param offset the number of data points to skip
     * @param limit the number of data points to return
     * @return a list of matching data points
     */
    // TODO confirm if HEAD handling needs anything additional
    // only allow clients with read scope to read data points
    @PreAuthorize("#oauth2.clientHasRole('" + CLIENT_ROLE + "') and #oauth2.hasScope('" + DATA_POINT_READ_SCOPE + "')")
    // TODO look into any meaningful @PostAuthorize filtering
    @RequestMapping(value = "/v1.0.M1/dataPoints", method = {HEAD, GET}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Iterable<DataPoint>> findDataPoints(
            @RequestParam(value = SCHEMA_NAMESPACE_PARAMETER) final String schemaNamespace,
            @RequestParam(value = SCHEMA_NAME_PARAMETER) final String schemaName,
            @RequestParam(value = SCHEMA_VERSION_PARAMETER, required = false) final String schemaVersion,
            @RequestParam(value = QUERY_PARAMETER, required = false) final String queryFilter,
            @RequestParam(value = END_USER_ID_PARAMETER, required = false) final String specifiedEndUserId,
            @RequestParam(value = CREATED_ON_OR_AFTER_PARAMETER, required = false)

            final OffsetDateTime createdOnOrAfter,
            @RequestParam(value = CREATED_BEFORE_PARAMETER, required = false) final OffsetDateTime createdBefore,
            @RequestParam(value = RESULT_OFFSET_PARAMETER, defaultValue = "0") final Integer offset,
            @RequestParam(value = RESULT_LIMIT_PARAMETER, defaultValue = DEFAULT_RESULT_LIMIT) final Integer limit,
            Authentication authentication) {

        // determine the user associated with the access token to restrict the search accordingly
        String endUserId = getEndUserId(authentication);

        DataPointSearchCriteria searchCriteria = new DataPointSearchCriteria();

        searchCriteria.setUserId(endUserId);
        searchCriteria.setSchemaNamespace(schemaNamespace);
        searchCriteria.setSchemaName(schemaName);
        searchCriteria.setSchemaVersionString(schemaVersion);
        searchCriteria.setCreatedOnOrAfter(createdOnOrAfter);
        searchCriteria.setCreatedBefore(createdBefore);

        // TODO add validation or explicitly comment that this is handled using exception translators
        if (!validator.validate(searchCriteria).isEmpty()) {
            // TODO add feedback
            return badRequest().body(null);
        }

        String combinedFilter = searchCriteria.asQueryFilter();
        if (queryFilter != null) {
            combinedFilter += " and " + queryFilter;
        }

        return findDataPointsM2(combinedFilter, specifiedEndUserId, offset, limit, authentication);
    }

    /**
     * Finds and retrieves datapoints, based on the passed in query.
     *
     * @param queryFilter an optional RSQL-formatted query to filter results with
     * @param offset      the number of data points to skip
     * @param limit       the number of data points to return
     * @return a list of matching data points
     */
    @PreAuthorize("#oauth2.clientHasRole('" + CLIENT_ROLE + "') and #oauth2.hasScope('" + DATA_POINT_READ_SCOPE + "')")
    // TODO look into any meaningful @PostAuthorize filtering
    @RequestMapping(value = "/v1.0.M2/dataPoints", method = {HEAD, GET}, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Iterable<DataPoint>> findDataPointsM2(
            @RequestParam(value = QUERY_PARAMETER, required = true) final String queryFilter,
            @RequestParam(value = END_USER_ID_PARAMETER, required = false) final String specifiedEndUserId,
            @RequestParam(value = RESULT_OFFSET_PARAMETER, defaultValue = "0") final Integer offset,
            @RequestParam(value = RESULT_LIMIT_PARAMETER, defaultValue = DEFAULT_RESULT_LIMIT) final Integer limit,
            Authentication authentication) {

        // determine the user associated with the access token to restrict the search accordingly
        String endUserId = getEndUserId(authentication); // FIXME
        Iterable<DataPoint> dataPoints = dataPointSearchService.findBySearchCriteria(queryFilter, offset, limit);

        return new ResponseEntity<>(dataPoints, new HttpHeaders(), OK);
    }

    public String getEndUserId(Authentication authentication) {

        return ((EndUserUserDetails) authentication.getPrincipal()).getUsername();
    }


}
