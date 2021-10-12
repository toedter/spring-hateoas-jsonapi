/*
 * Copyright 2021 the original author or authors.
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;

import java.util.List;
import java.util.Map;

class JsonApiPagedModelDeserializer extends AbstractJsonApiModelDeserializer<PagedModel<?>>
        implements ContextualDeserializer {

    private final ObjectMapper mapper = new ObjectMapper();

    public JsonApiPagedModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
        super(jsonApiConfiguration);
    }

    protected JsonApiPagedModelDeserializer(JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
        super(contentType, jsonApiConfiguration);
    }

    @Override
    protected PagedModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
        Links links = doc.getLinks();

        PagedModel.PageMetadata pageMetadata = null;
        if (doc.getMeta() != null) {
            Map<String, Long> pageMeta = (Map<String, Long>) doc.getMeta().get("page");
            if (pageMeta != null) {
                pageMetadata = new PagedModel.PageMetadata(
                        ((Number) pageMeta.get("size")).longValue(),
                        ((Number) pageMeta.get("number")).longValue(),
                        ((Number) pageMeta.get("totalElements")).longValue(),
                        ((Number) pageMeta.get("totalPages")).longValue()
                );
            }
        }

        return PagedModel.of(resources, pageMetadata, links);
    }

    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
        return new JsonApiPagedModelDeserializer(type, jsonApiConfiguration);
    }
}
