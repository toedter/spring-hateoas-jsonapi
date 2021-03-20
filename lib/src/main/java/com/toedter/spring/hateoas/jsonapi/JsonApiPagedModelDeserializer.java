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
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;

import java.util.List;

class JsonApiPagedModelDeserializer extends AbstractJsonApiModelDeserializer<PagedModel<?>>
        implements ContextualDeserializer {

    public JsonApiPagedModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
        super(jsonApiConfiguration);
    }

    protected JsonApiPagedModelDeserializer(JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
        super(contentType, jsonApiConfiguration);
    }

    @Override
    protected PagedModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
        Links links = doc.getLinks();

        return PagedModel.of(resources, null, links);
    }

    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
        return new JsonApiPagedModelDeserializer(type, jsonApiConfiguration);
    }
}
