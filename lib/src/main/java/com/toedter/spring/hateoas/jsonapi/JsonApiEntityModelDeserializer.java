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

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static com.toedter.spring.hateoas.jsonapi.ReflectionUtils.getAllDeclaredFields;

class JsonApiEntityModelDeserializer extends AbstractJsonApiModelDeserializer<EntityModel<?>>
        implements ContextualDeserializer {

    public static final String CANNOT_DESERIALIZE_INPUT_TO_ENTITY_MODEL = "Cannot deserialize input to EntityModel";

    public JsonApiEntityModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
        super(jsonApiConfiguration);
    }

    protected JsonApiEntityModelDeserializer(JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
        super(contentType, jsonApiConfiguration);
    }

    @Override
    protected EntityModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
        Assert.notNull(doc, "JsonApiDocument must not be null!");
        Links links = doc.getLinks();
        if (resources.size() == 1) {
            EntityModel<Object> entityModel = EntityModel.of(resources.get(0));
            if (links != null) {
                entityModel.add(links);
            }

            if (doc.getData() == null) {
                return entityModel;
            }

            HashMap<String, Object> relationships =
                    (HashMap<String, Object>) ((HashMap<String, Object>) doc.getData()).get("relationships");

            Object content = entityModel.getContent();
            if (relationships != null) {

                @SuppressWarnings("ConstantConditions") final Field[] declaredFields = getAllDeclaredFields(content.getClass());
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    JsonApiRelationships relationshipsAnnotation = field.getAnnotation(JsonApiRelationships.class);
                    if (relationshipsAnnotation != null) {
                        Object relationship = relationships.get(relationshipsAnnotation.value());
                        try {
                            if (relationship != null) {
                                final Type genericType = field.getGenericType();
                                // expect collections to always be generic, like "List<Director>"
                                if (genericType instanceof ParameterizedType) {
                                    ParameterizedType type = (ParameterizedType) genericType;
                                    if (Collection.class.isAssignableFrom(field.getType())) {
                                        Collection<Object> relationshipCollection;
                                        if (Set.class.isAssignableFrom(field.getType())) {
                                            relationshipCollection = new HashSet<>();
                                        } else {
                                            relationshipCollection = new ArrayList<>();
                                        }
                                        Object data = ((HashMap<?, ?>) relationship).get("data");
                                        List<HashMap<String, String>> jsonApiRelationships;
                                        if (data instanceof List) {
                                            jsonApiRelationships = (List<HashMap<String, String>>) data;
                                        } else if (data instanceof HashMap) {
                                            HashMap<String, String> castedData = (HashMap<String, String>) data;
                                            jsonApiRelationships = Collections.singletonList(castedData);
                                        } else {
                                            throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_ENTITY_MODEL);
                                        }
                                        Type typeArgument = type.getActualTypeArguments()[0];

                                        for (HashMap<String, String> entry : jsonApiRelationships) {
                                            if (jsonApiConfiguration.isTypeForClassUsedForDeserialization()) {
                                                Class<?> entryType = jsonApiConfiguration.getClassForType(entry.get("type"));
                                                if (entryType != null) {
                                                    Class<?> clazz = (Class<?>) typeArgument;
                                                    if (!clazz.isAssignableFrom(entryType)) {
                                                        throw new IllegalArgumentException(entryType + " is not assignable to "
                                                                + typeArgument + ".");
                                                    }
                                                    typeArgument = entryType;
                                                }
                                            }
                                            Object newInstance = objectMapper.convertValue(entry, objectMapper.constructType(typeArgument));

                                            JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                                                    newInstance, JsonApiResourceIdentifier.JsonApiResourceField.id, entry.get("id"));
                                            JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                                                    newInstance, JsonApiResourceIdentifier.JsonApiResourceField.type, entry.get("type"));
                                            relationshipCollection.add(newInstance);
                                        }

                                        field.set(content, relationshipCollection);
                                    }
                                } else {
                                    // we expect a concrete type otherwise, like "Director"
                                    Class<?> clazz = Class.forName(genericType.getTypeName());
                                    Object newInstance = clazz.getDeclaredConstructor().newInstance();
                                    HashMap<String, Object> data =
                                            (HashMap<String, Object>) ((HashMap<?, ?>) relationship).get("data");
                                    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                                            newInstance, JsonApiResourceIdentifier.JsonApiResourceField.id, data.get("id").toString());
                                    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
                                            newInstance, JsonApiResourceIdentifier.JsonApiResourceField.type, data.get("type").toString());
                                    field.set(content, newInstance);
                                }
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_ENTITY_MODEL, e);
                        }
                    }
                }
            }

            // handling meta deserialization
            Object meta = ((HashMap<?, ?>) doc.getData()).get("meta");
            if (meta != null) {
                for (Field field : content.getClass().getDeclaredFields()) {
                    if (field.getAnnotation(JsonApiMeta.class) != null) {
                        try {
                            field.setAccessible(true);
                            if (meta instanceof Map) {
                                Object metaValue = ((Map<?,?>) meta).get(field.getName());
                                if (metaValue != null) {
                                    field.set(content, metaValue);
                                }
                            }
                        } catch (IllegalAccessException e) {
                            throw new IllegalArgumentException("Cannot set JSON:API meta data for annotated field: "
                                    + field.getName(), e);
                        }
                    }
                }
                for (Method method : content.getClass().getDeclaredMethods()) {
                    if (method.getAnnotation(JsonApiMeta.class) != null) {
                        try {
                            method.setAccessible(true);
                            // a setter is expected to return void
                            if (method.getReturnType() == void.class) {
                                method.invoke(content);
                            }
                        } catch (Exception e) {
                            throw new IllegalArgumentException("Cannot set JSON:API meta data for annotated method: "
                                    + method.getName(), e);
                        }
                    }
                }
            }

            return entityModel;
        }
        throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_ENTITY_MODEL);

    }

    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
        return new JsonApiEntityModelDeserializer(type, jsonApiConfiguration);
    }
}
