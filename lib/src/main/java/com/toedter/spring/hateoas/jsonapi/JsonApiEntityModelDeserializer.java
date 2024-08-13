/*
 * Copyright 2023 the original author or authors.
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
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.toedter.spring.hateoas.jsonapi.ReflectionUtils.getAllDeclaredFields;

@SuppressWarnings("squid:S3011")
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
                                if (genericType instanceof ParameterizedType parameterizedType) {
                                    ParameterizedType type = parameterizedType;
                                    if (Collection.class.isAssignableFrom(field.getType())) {
                                        Collection<Object> relationshipCollection;
                                        if (Set.class.isAssignableFrom(field.getType())) {
                                            relationshipCollection = new HashSet<>();
                                        } else {
                                            relationshipCollection = new ArrayList<>();
                                        }
                                        Object data = ((HashMap<?, ?>) relationship).get("data");
                                        List<HashMap<String, Object>> jsonApiRelationships = null;
                                        if (data instanceof List) {
                                            jsonApiRelationships = (List<HashMap<String, Object>>) data;
                                        } else if (data instanceof HashMap) {
                                            HashMap<String, Object> castedData = (HashMap<String, Object>) data;
                                            jsonApiRelationships = Collections.singletonList(castedData);
                                        } else if (data != null) {
                                            throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_ENTITY_MODEL);
                                        }

                                        if (data != null) {
                                            Type typeArgument = type.getActualTypeArguments()[0];

                                            for (HashMap<String, Object> entry : jsonApiRelationships) {
                                                Object newInstance = createRelationship(doc, typeArgument, entry).getContent();
                                                relationshipCollection.add(newInstance);
                                            }
                                        }

                                        field.set(content, relationshipCollection);
                                    }
                                } else {
                                    // we expect a concrete type otherwise, like "Director"
                                    HashMap<String, Object> data =
                                            (HashMap<String, Object>) ((HashMap<?, ?>) relationship).get("data");
                                    Object newInstance = createRelationship(doc, genericType, data).getContent();
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
                for (Field field : getAllDeclaredFields(content.getClass())) {
                    if (field.getAnnotation(JsonApiMeta.class) != null) {
                        try {
                            field.setAccessible(true);
                            if (meta instanceof Map) {
                                Object metaValue = ((Map<?, ?>) meta).get(field.getName());
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
                            if (method.getReturnType() == void.class && meta instanceof Map) {
                                String methodName = method.getName();
                                if (methodName.startsWith("set")) {
                                    methodName = StringUtils.uncapitalize(methodName.substring(3));
                                }

                                Object metaValue = ((Map<?, ?>) meta).get(methodName);
                                if (metaValue != null) {
                                    method.invoke(content, metaValue);
                                }
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

    @Nullable
    private EntityModel<?> createRelationship(JsonApiDocument doc, Type typeArgument, HashMap<String, Object> entry) {
        String id = entry.get("id").toString();
        String jsonApiType = entry.get("type").toString();
        if (doc != null && doc.getIncluded() != null) {
            Map<String, Object> attributes = findIncludedAttributesForRelationshipObject(id, jsonApiType, doc);
            if (attributes != null) {
                entry.put("attributes", attributes);
            }
            Map<String, Object> relationships = findIncludedRelationshipsForRelationshipObject(id, jsonApiType, doc);
            if (relationships != null) {
                entry.put("relationships", relationships);
            }
        }
        return (EntityModel<?>) convertToResource(entry, true, doc, objectMapper.constructType(typeArgument), true);
    }

    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
        return new JsonApiEntityModelDeserializer(type, jsonApiConfiguration);
    }

    protected @Nullable Map<String, Object> findIncludedAttributesForRelationshipObject(
            String id, String type, JsonApiDocument doc) {
        for (JsonApiData jsonApiData : doc.getIncluded()) {
            if (id.equals(jsonApiData.getId()) && type.equals(jsonApiData.getType())) {
                return jsonApiData.getAttributes();
            }
        }
        return null;
    }

    protected @Nullable Map<String, Object> findIncludedRelationshipsForRelationshipObject(
            String id, String type, JsonApiDocument doc) {

        for (JsonApiData jsonApiData : doc.getIncluded()) {
            if (id.equals(jsonApiData.getId()) && type.equals(jsonApiData.getType())) {
                return (Map<String, Object>) jsonApiData.getRelationships();
            }
        }
        return null;
    }
}
