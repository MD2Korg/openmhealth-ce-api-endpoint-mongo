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

package org.openmhealth.dsu.domain;

import com.google.common.collect.Range;
import org.openmhealth.dsu.validation.ValidDataPointSearchCriteria;
import org.openmhealth.dsu.validation.ValidSchemaName;
import org.openmhealth.dsu.validation.ValidSchemaNamespace;
import org.openmhealth.dsu.validation.ValidSchemaVersion;
import org.openmhealth.schema.domain.omh.SchemaVersion;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.google.common.collect.BoundType.CLOSED;
import static java.lang.String.format;


/**
 * A bean that represents a search for data points.
 *
 * @author Emerson Farrugia
 */
@ValidDataPointSearchCriteria
public class DataPointSearchCriteria {

    private String userId;
    private String schemaNamespace;
    private String schemaName;
    private String schemaVersion;
    private OffsetDateTime createdOnOrAfter;
    private OffsetDateTime createdBefore;
    private OffsetDateTime effectiveOnOrAfter;
    private OffsetDateTime effectiveBefore;
    private String acquisitionSourceId; // TODO confirm if we want to run with this name


    /**
     * @return the user the data points belong to
     */
    @NotNull
    @Size(min = 1)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return the schema namespace of the body of the data points
     */
    @NotNull
    @ValidSchemaNamespace
    public String getSchemaNamespace() {
        return schemaNamespace;
    }

    public void setSchemaNamespace(String schemaNamespace) {
        this.schemaNamespace = schemaNamespace;
    }

    /**
     * @return the schema name of the body of the data points
     */
    @NotNull
    @ValidSchemaName
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    /**
     * @return the schema version of the body of the data points, as a string
     */
    @ValidSchemaVersion
    public Optional<String> getSchemaVersionString() {
        return Optional.ofNullable(schemaVersion);
    }

    public void setSchemaVersionString(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    /**
     * @return the schema version of the body of the data points
     */
    public Optional<SchemaVersion> getSchemaVersion() {

        return getSchemaVersionString().map(SchemaVersion::new);
    }

    /**
     * @return the oldest creation timestamp of the data points
     */
    public Optional<OffsetDateTime> getCreatedOnOrAfter() {
        return Optional.ofNullable(createdOnOrAfter);
    }

    public void setCreatedOnOrAfter(OffsetDateTime createdOnOrAfter) {
        this.createdOnOrAfter = createdOnOrAfter;
    }

    /**
     * @return the newest creation timestamp of the data points
     */
    public Optional<OffsetDateTime> getCreatedBefore() {
        return Optional.ofNullable(createdBefore);
    }

    public void setCreatedBefore(OffsetDateTime createdBefore) {
        this.createdBefore = createdBefore;
    }

    /**
     * @return the oldest effective timestamp of the data points
     */
    public Optional<OffsetDateTime> getEffectiveOnOrAfter() {
        return Optional.ofNullable(effectiveOnOrAfter);
    }

    public void setEffectiveOnOrAfter(OffsetDateTime effectiveOnOrAfter) {
        this.effectiveOnOrAfter = effectiveOnOrAfter;
    }

    /**
     * @return the newest effective timestamp of the data points
     */
    public Optional<OffsetDateTime> getEffectiveBefore() {
        return Optional.ofNullable(effectiveBefore);
    }

    public void setEffectiveBefore(OffsetDateTime effectiveBefore) {
        this.effectiveBefore = effectiveBefore;
    }

    /**
     * @return the creation timestamp range of the data points
     */
    public Range<OffsetDateTime> getCreationTimestampRange() {
        return asRange(createdOnOrAfter, createdBefore);
    }

    /**
     * @return the effective timestamp range of the data points
     */
    public Range<OffsetDateTime> getEffectiveTimestampRange() {
        return asRange(effectiveOnOrAfter, effectiveBefore);
    }

    /**
     * @return the identifier of the acquisition source
     */
    @Size(min = 1)
    public Optional<String> getAcquisitionSourceId() {
        return Optional.ofNullable(acquisitionSourceId);
    }

    public void setAcquisitionSourceId(String acquisitionSourceId) {
        this.acquisitionSourceId = acquisitionSourceId;
    }

    /**
     * @return a closed-open range corresponding to the specified date times
     */
    protected Range<OffsetDateTime> asRange(OffsetDateTime onOrAfterDateTime, OffsetDateTime beforeDateTime) {

        if (onOrAfterDateTime != null && beforeDateTime != null) {
            return Range.closedOpen(onOrAfterDateTime, beforeDateTime);
        }

        if (onOrAfterDateTime != null) {
            return Range.atLeast(onOrAfterDateTime);
        }
        else if (beforeDateTime != null) {
            return Range.lessThan(beforeDateTime);
        }

        return Range.all();
    }

    public String asQueryFilter() {
        String result = format("header.user_id == '%s' and header.schema_id.namespace == '%s' and header.schema_id.name == '%s'",
                getUserId(), getSchemaNamespace(), getSchemaName());

        if (getSchemaVersion().isPresent()) {
            SchemaVersion version = getSchemaVersion().get();

            result += format(" and header.schema_id.version.major == %d and header.schema_id.version.minor == %d", version.getMajor(), version.getMinor());

            if (version.getQualifier().isPresent()) {
                result += format(" and header.schema_id.version.qualifier == '%s'", version.getQualifier().get());
            } else {
                result += " and header.schema_id.version.qualifier =ex= false";
            }
        }

        result += addCreationTimestampCriteria(getCreationTimestampRange());

        return result;


    }

    String addCreationTimestampCriteria(Range<OffsetDateTime> timestampRange) {

        String result = "";

        if (timestampRange.hasLowerBound() || timestampRange.hasUpperBound()) {


            result = " and header.creation_date_time";

            if (timestampRange.hasLowerBound()) {
                result += ((timestampRange.lowerBoundType() == CLOSED) ? " >= " : " > ") + format("'%s'", timestampRange.lowerEndpoint());
            }

            if (timestampRange.hasUpperBound()) {
                result += ((timestampRange.upperBoundType() == CLOSED) ? " <= " : " < ") + format("'%s'", timestampRange.upperEndpoint().toEpochSecond());
            }

        }
        return result;
    }

}
