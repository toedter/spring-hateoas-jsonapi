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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

abstract class AbstractJsonApiModelDeserializer<T> extends ContainerDeserializerBase<T>
        implements ContextualDeserializer {

    protected final JavaType contentType;

    AbstractJsonApiModelDeserializer() {
        this(TypeFactory.defaultInstance().constructSimpleType(JsonApiDocument.class, new JavaType[0]));
    }

    protected AbstractJsonApiModelDeserializer(JavaType contentType) {
        super(contentType);
        this.contentType = contentType;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonApiDocument doc = p.getCodec().readValue(p, JsonApiDocument.class);
        if (doc.getData() != null && doc.getData() instanceof Collection<?>) {
            List<HashMap<String, Object>> collection = (List<HashMap<String, Object>>) doc.getData();
            List<Object> resources = collection.stream()
                    .map(this::convertToResource)
                    .collect(Collectors.toList());
            return convertToRepresentationModel(resources, doc);
        }
        HashMap<String, Object> data = (HashMap<String, Object>) doc.getData();
        final Object objectFromProperties = convertToResource(data);
        return convertToRepresentationModel(Collections.singletonList(objectFromProperties), doc);
    }

    private Object convertToResource(HashMap<String, Object> data) {
        if (data == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
        JavaType rootType = JacksonHelper.findRootType(this.contentType);
        ObjectMapper mapper = new ObjectMapper();
        final Object objectFromProperties = mapper.convertValue(attributes, rootType.getRawClass());    
        JsonApiResource.setJsonApiResourceFieldAttributeForObject(objectFromProperties, JsonApiResource.JsonApiResourceField.id, (String) data.get("id"));
        JsonApiResource.setJsonApiResourceFieldAttributeForObject(objectFromProperties, JsonApiResource.JsonApiResourceField.type, (String) data.get("type"));
        return objectFromProperties;
    }

    abstract protected T convertToRepresentationModel(List<Object> resources, JsonApiDocument doc);

    @Override
    public JavaType getContentType() {
        return this.contentType;
    }

    @Override
    @Nullable
    public JsonDeserializer<Object> getContentDeserializer() {
        return null;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();
        return createJsonDeserializer(type);
    }

    abstract protected JsonDeserializer<?> createJsonDeserializer(JavaType type);
}
