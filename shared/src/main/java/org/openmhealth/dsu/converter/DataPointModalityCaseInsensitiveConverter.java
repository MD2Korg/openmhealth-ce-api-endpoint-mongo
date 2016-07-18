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

package org.openmhealth.dsu.converter;

import org.openmhealth.schema.domain.omh.DataPointModality;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;


/**
 * HACK ALERT - FIXME: we shouldn't be doing this here, and not just for this type
 *
 * @author Wallace Wadge
 */
@Component
@ReadingConverter
public class DataPointModalityCaseInsensitiveConverter implements Converter<String, DataPointModality> {

    @Override
    public DataPointModality convert(String source) {

        if (source == null) {
            return null;
        }

        return Enum.valueOf(DataPointModality.class, source.toUpperCase());
    }
}
