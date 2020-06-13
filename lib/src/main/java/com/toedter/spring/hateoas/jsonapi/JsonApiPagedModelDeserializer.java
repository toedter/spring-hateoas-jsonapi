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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Map;

class JsonApiPagedModelDeserializer extends AbstractJsonApiModelDeserializer<PagedModel<?>>
        implements ContextualDeserializer {

    JsonApiPagedModelDeserializer() {
        super();
    }

    protected JsonApiPagedModelDeserializer(JavaType contentType) {
        super(contentType);
    }

    @Override
    protected PagedModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
        Links links = doc.getLinks();

        Map<String, Object> metaMap = doc.getMeta();
        @SuppressWarnings("unchecked")
        Map<String, Object> page = (Map<String, Object>) metaMap.get("page");
        long size = new Long(page.get(Jackson2JsonApiModule.PAGE_SIZE).toString());
        long number = new Long(page.get(Jackson2JsonApiModule.PAGE_NUMBER).toString());
        long totalElements = new Long(page.get(Jackson2JsonApiModule.PAGE_TOTAL_ELEMENTS).toString());
        long totalPages = new Long(page.get(Jackson2JsonApiModule.PAGE_TOTAL_PAGES).toString());

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(size, number, totalElements, totalPages);

        return PagedModel.of(resources, pageMetadata, links);
    }

    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
        return new JsonApiPagedModelDeserializer(type);
    }
}
