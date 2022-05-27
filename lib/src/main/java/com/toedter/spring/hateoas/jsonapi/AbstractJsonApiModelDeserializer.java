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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
abstract class AbstractJsonApiModelDeserializer<T> extends ContainerDeserializerBase<T>
        implements ContextualDeserializer {

    protected final ObjectMapper objectMapper;
    protected final JavaType contentType;
    protected final JsonApiConfiguration jsonApiConfiguration;

    private final ObjectMapper plainObjectMapper;

    AbstractJsonApiModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
        this(TypeFactory.defaultInstance().constructSimpleType(JsonApiDocument.class, new JavaType[0]),
                jsonApiConfiguration);
    }

    protected AbstractJsonApiModelDeserializer(JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
        super(contentType);
        this.contentType = contentType;
        this.jsonApiConfiguration = jsonApiConfiguration;
        this.objectMapper = jsonApiConfiguration.getObjectMapper();

        plainObjectMapper = new ObjectMapper();
        jsonApiConfiguration.customize(plainObjectMapper);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        boolean isEntityModelCollection = false;
        if (this instanceof JsonApiPagedModelDeserializer || this instanceof JsonApiCollectionModelDeserializer) {
            JavaType javaType = contentType.containedType(0);
            if (javaType.getRawClass() == EntityModel.class) {
                isEntityModelCollection = true;
            }
        }
        JsonApiDocument doc = p.getCodec().readValue(p, JsonApiDocument.class);
        if (doc.getData() != null && doc.getData() instanceof Collection<?>) {
            final boolean isEntityModelCollectionFinal = isEntityModelCollection;
            List<HashMap<String, Object>> collection = (List<HashMap<String, Object>>) doc.getData();
            List<Object> resources = collection.stream()
                    .map(data -> this.convertToResource(data, isEntityModelCollectionFinal, doc, null, false))
                    .collect(Collectors.toList());
            return convertToRepresentationModel(resources, doc);
        }
        HashMap<String, Object> data = (HashMap<String, Object>) doc.getData();
        final Object objectFromProperties = convertToResource(data, false, doc, null, false);

        return convertToRepresentationModel(Collections.singletonList(objectFromProperties), doc);
    }

    @Nullable
    Object convertToResource(@Nullable HashMap<String, Object> data, boolean wrapInEntityModel,
                             @Nullable JsonApiDocument doc, @Nullable JavaType javaType, boolean useDataForCreation) {
        if (data == null) {
            return null;
        }

        Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");

        Object objectFromProperties;
        JavaType rootType = javaType;
        if (rootType == null) {
            rootType = JacksonHelper.findRootType(this.contentType);
        }
        Class<?> clazz = null;

        if (jsonApiConfiguration.isTypeForClassUsedForDeserialization()) {
            String jsonApiType = (String) data.get("type");
            if (jsonApiType != null) {
                clazz = jsonApiConfiguration.getClassForType(jsonApiType);
                if (clazz != null && !rootType.getRawClass().isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException(clazz + " is not assignable to " + rootType.getRawClass());
                }
                if (clazz != null) {
                    rootType = objectMapper.constructType(clazz);
                }
            }
        }

        if (attributes != null) {
            // we have to use the plain object mapper to not get in conflict with links deserialization
            objectFromProperties = plainObjectMapper.convertValue(attributes, rootType);
        } else {
            try {
                if (useDataForCreation) {
                    // we have to use the "real" object mapper due to polymorphic deserialization using Jackson
                    objectFromProperties = objectMapper.convertValue(data, rootType);
                } else {
                    if (clazz == null) {
                        clazz = rootType.getRawClass();
                    }
                    objectFromProperties = clazz.getDeclaredConstructor().newInstance();
                }
            } catch (Exception e) {
                throw new IllegalStateException("Cannot convert data to resource.");
            }
        }

        JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                objectFromProperties, JsonApiResourceIdentifier.JsonApiResourceField.ID, (String) data.get("id"));
        JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                objectFromProperties, JsonApiResourceIdentifier.JsonApiResourceField.TYPE, (String) data.get("type"));

        if (wrapInEntityModel) {
            Links links = this.objectMapper.convertValue(data.get("links"), Links.class);
            if (links == null) {
                links = Links.NONE;
            }
            JsonApiEntityModelDeserializer jsonApiEntityModelDeserializer =
                    new JsonApiEntityModelDeserializer(jsonApiConfiguration);
            JsonApiDocument jsonApiDocument =
                    new JsonApiDocument(null, data, null, null, links,
                            doc != null ? doc.getIncluded() : null);
            return jsonApiEntityModelDeserializer.convertToRepresentationModel(
                    Collections.singletonList(objectFromProperties), jsonApiDocument);
        }

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
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, @Nullable BeanProperty property) {
        JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();
        return createJsonDeserializer(type);
    }

    abstract protected JsonDeserializer<?> createJsonDeserializer(JavaType type);
}
