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

package org.openmhealth.dsu.repository;

import com.github.rutledgepaulv.qbuilders.builders.GeneralQueryBuilder;
import com.github.rutledgepaulv.qbuilders.conditions.Condition;
import com.github.rutledgepaulv.qbuilders.structures.FieldPath;
import com.github.rutledgepaulv.qbuilders.visitors.MongoVisitor;
import com.github.rutledgepaulv.rqe.conversions.StringToTypeConverter;
import com.github.rutledgepaulv.rqe.conversions.parsers.StringToInstantConverter;
import com.github.rutledgepaulv.rqe.conversions.parsers.StringToObjectBestEffortConverter;
import com.github.rutledgepaulv.rqe.pipes.DefaultArgumentConversionPipe;
import com.github.rutledgepaulv.rqe.pipes.QueryConversionPipeline;
import com.github.rutledgepaulv.rqe.resolvers.MongoPersistentEntityFieldTypeResolver;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;


/**
 * @author Emerson Farrugia
 */
public class MongoDataPointRepositoryImpl implements DataPointSearchRepositoryCustom {

    @Autowired
    private MongoOperations mongoOperations;


    private QueryConversionPipeline pipeline = QueryConversionPipeline.builder()

            .useNonDefaultArgumentConversionPipe(DefaultArgumentConversionPipe
                    .builder()
                    .useNonDefaultStringToTypeConverter(new CustomStringToTypeConverter())
                    .useNonDefaultFieldResolver(new MongoPersistentEntityFieldTypeResolver() {
                        @Override
                        public Class<?> apply(FieldPath path, Class<?> root) {
                            return Object.class;
                        }
                    })
                    .build()
            ).build();



    // if a data point is filtered by its data and not just its header, these queries will need to be written using
    // the MongoDB Java driver instead of Spring Data MongoDB, since there is no mapping information to work against
    @Override
    public Iterable<DataPoint> findBySearchCriteria(String queryFilter, @Nullable Integer offset,
                                                    @Nullable Integer limit) {


        checkNotNull(queryFilter);
        checkArgument(offset == null || offset >= 0);
        checkArgument(limit == null || limit >= 0);


        Query query = newQuery(queryFilter);
        if (offset != null) {
            query.skip(offset);
        }

        if (limit != null) {
            query.limit(limit);
        }

        return mongoOperations.find(query, DataPoint.class);
    }

    @Override
    public Iterable<String> findParticipantsBySearchCriteria(String queryFilter) {

        checkNotNull(queryFilter);

        Condition<GeneralQueryBuilder> condition = pipeline.apply(queryFilter.replace("&&", ";").replace("||", ","), DataPoint.class);
        Criteria criteria = condition.query(new MongoVisitor());


        AggregationResults<AggResult> groupResults = mongoOperations.aggregate(
                newAggregation(match(criteria),
                        unwind("header.userId"),
                        group(fields("header.userId")).addToSet("header.userId").as("user_id")), DataPoint.class, AggResult.class);
        return groupResults.getMappedResults().stream().map(dp -> dp.getUser_id()).collect(Collectors.toSet());
    }




    private Query newQuery(String queryFilter) {
        Query query = new Query();
        if (queryFilter != null) {
            Condition<GeneralQueryBuilder> condition = pipeline.apply(queryFilter.replace("&&", ";").replace("||", ","), DataPoint.class);
            query.addCriteria(condition.query(new MongoVisitor()));
        }

        return query;
    }


    private class CustomStringToTypeConverter implements StringToTypeConverter {
        private ConversionService conversionService;

        public CustomStringToTypeConverter() {
            DefaultConversionService conversions = new DefaultConversionService();
            conversions.addConverter(new StringToInstantConverter());
            conversions.addConverter(new StringToObjectBestEffortConverter() {
                @Override
                public Object convert(String source) {
                    try {  // overridden because we're using OffsetDateTime, not Date
                        return OffsetDateTime.parse(source);
                    } catch (Exception e) {
                        return super.convert(source);
                    }
                }
            });
            this.conversionService = conversions;
        }

        @Override
        public boolean supports(Class<?> clazz) {
            return conversionService.canConvert(String.class, clazz);
        }

        @Override
        public Object apply(String s, Class<?> aClass) {
            return conversionService.convert(s, aClass);
        }


    }

    class AggResult {
        String id;
        Set<String> user_id;

        public String getUser_id() {
            return user_id.iterator().next();
        }
    }
}