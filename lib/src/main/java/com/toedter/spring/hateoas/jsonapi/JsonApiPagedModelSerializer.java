/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.toedter.spring.hateoas.jsonapi;

import org.springframework.hateoas.PagedModel;

import java.util.HashMap;
import java.util.Map;

class JsonApiPagedModelSerializer extends AbstractJsonApiRepresentationModelSerializer<PagedModel<?>> {
    public JsonApiPagedModelSerializer() {
        super(PagedModel.class, false);
    }

    @Override
    protected JsonApiDocument postProcess(PagedModel<?> value, JsonApiDocument doc) {
        if (value.getMetadata() != null) {
            Map<String, Object> metaMap = new HashMap<>();
            metaMap.put(Jackson2JsonApiModule.PAGE_NUMBER, value.getMetadata().getNumber());
            metaMap.put(Jackson2JsonApiModule.PAGE_SIZE, value.getMetadata().getSize());
            metaMap.put(Jackson2JsonApiModule.PAGE_TOTAL_ELEMENTS, value.getMetadata().getTotalElements());
            metaMap.put(Jackson2JsonApiModule.PAGE_TOTAL_PAGES, value.getMetadata().getTotalPages());
            doc = doc.withMeta(metaMap);
        }
        return doc;
    }
}
