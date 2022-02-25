/*
 * Copyright 2022 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.Getter;
import org.springframework.hateoas.Links;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

class JsonApiRelationshipSerializer extends AbstractJsonApiSerializer<JsonApiRelationship> {
    private final JsonApiConfiguration jsonApiConfiguration;

    @Getter
    private static class JsonApiRelationshipForSerialization {
        private final Object data;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final Links links;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        private final Map<String, Object> meta;

        public JsonApiRelationshipForSerialization(Object data, Links links, Map<String, Object> meta) {
            this.data = data;
            this.links = links;
            this.meta = meta;
        }
    }

    public JsonApiRelationshipSerializer(JsonApiConfiguration jsonApiConfiguration) {
        super(JsonApiRelationship.class, false);
        this.jsonApiConfiguration = jsonApiConfiguration;
    }

    @Override
    public void serialize(JsonApiRelationship value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Object data = value.getData();
        if (data instanceof Collection) {
            data = value.toJsonApiResourceCollection((Collection<?>) data, jsonApiConfiguration);
        } else {
            data = value.toJsonApiResource(data, jsonApiConfiguration);
        }
        JsonApiRelationshipForSerialization jsonApiRelationship =
                new JsonApiRelationshipForSerialization(data, value.getLinks(), value.getMeta());

        provider
                .findValueSerializer(JsonApiRelationshipForSerialization.class)
                .serialize(jsonApiRelationship, gen, provider);
    }
}
