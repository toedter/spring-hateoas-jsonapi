/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.toedter.spring.hateoas.jsonapi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import org.springframework.hateoas.*;

import java.io.IOException;

public class Jackson2JsonApiModule extends SimpleModule {
    public Jackson2JsonApiModule() {

        super("json-api-module",
                new Version(1, 0, 0, null,
                        "com.toedter",
                        "jsonapi-spring-hateoas"));

        addSerializer(new JsonApiEntityModelSerializer());
        addSerializer(new JsonApiRepresentationModelSerializer());
        addSerializer(new JsonApiCollectionModelSerializer());
        addSerializer(new JsonApiLinksSerializer());
    }

    static abstract class AbstractJsonApiSerializer<T> extends ContainerSerializer<T> {

        protected AbstractJsonApiSerializer(Class<T> t) {
            super(t);
        }

        protected AbstractJsonApiSerializer(Class<?> t, boolean dummy) {
            super(t, dummy);
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean hasSingleElement(T value) {
            return false;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

    static class JsonApiEntityModelSerializer extends AbstractJsonApiSerializer<EntityModel<?>> {
        public JsonApiEntityModelSerializer() {
            super(EntityModel.class, false);
        }

        @Override
        public void serialize(EntityModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiRepresentationModelSerializer extends AbstractJsonApiSerializer<RepresentationModel<?>> {
        public JsonApiRepresentationModelSerializer() {
            super(RepresentationModel.class, false);
        }

        @Override
        public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiCollectionModelSerializer extends AbstractJsonApiSerializer<CollectionModel<?>> {
        public JsonApiCollectionModelSerializer() {
            super(CollectionModel.class, false);
        }

        @Override
        public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiLinksSerializer extends AbstractJsonApiSerializer<Links> {
        public JsonApiLinksSerializer() {
            super(Links.class);
        }

        @Override
        public void serialize(Links value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            for (Link link : value) {
                gen.writeStringField(link.getRel().value(), link.getHref());
            }
            gen.writeEndObject();
        }
    }
}
