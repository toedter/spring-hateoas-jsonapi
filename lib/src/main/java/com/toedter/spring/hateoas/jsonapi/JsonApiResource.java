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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.atteo.evo.inflector.English;
import org.springframework.hateoas.EntityModel;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;


@Value
@Getter(onMethod_ = {@JsonProperty})
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonApiResource {
    public static final String JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID =
            "JSON:API resource object must have property \"id\".";
    public static final String JSONAPI_ID_ANNOTATION = "com.toedter.spring.hateoas.jsonapi.JsonApiId";
    public static final String JSONAPI_TYPE_ANNOTATION = "com.toedter.spring.hateoas.jsonapi.JsonApiType";
    public static final String JPA_ID_ANNOTATION = "javax.persistence.Id";

    Object id;
    String type;

    @JsonCreator
    public JsonApiResource(
            @JsonProperty("id") Object id,
            @JsonProperty("type") String type
    ) {
        this.id = id;
        this.type = type;
    }

    static class ResourceField {
        String name;
        String value;

        public ResourceField(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    /**
     * Creates a JSON:API resource from an entity model
     *
     * @param entityModel the base for the relationship
     * @return the JSON:API resource
     */
    public static JsonApiResource of(EntityModel<?> entityModel) {
        final Object content = entityModel.getContent();
        final JsonApiConfiguration jsonApiConfiguration = new JsonApiConfiguration();
        Object id = JsonApiResource.getId(content, jsonApiConfiguration).value;
        String type = JsonApiResource.getType(content, jsonApiConfiguration).value;
        return new JsonApiResource(id, type);
    }

    static ResourceField getId(Object object, JsonApiConfiguration jsonApiConfiguration) {
        return getResourceField(JsonApiResourceField.id, object, jsonApiConfiguration);
    }

    static ResourceField getType(Object object, JsonApiConfiguration jsonApiConfiguration) {
        return getResourceField(JsonApiResourceField.type, object, jsonApiConfiguration);
    }

    enum JsonApiResourceField {id, type}

    static private ResourceField getResourceField(
            JsonApiResourceField resourceField, Object object, JsonApiConfiguration jsonApiConfiguration) {

        try {
            // first search for field annotation
            final Field[] declaredFields = object.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if (resourceField == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))) {
                        return new ResourceField(field.getName(), field.get(object).toString());
                    } else if (resourceField == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName)) {
                        return new ResourceField(field.getName(), field.get(object).toString());
                    }
                }
            }

            // first search for method annotation
            final Method[] declaredMethods = object.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                final Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    boolean isAnnotatedMethod = false;
                    if (resourceField == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))) {
                        isAnnotatedMethod = true;
                    } else if (resourceField == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName)) {
                        isAnnotatedMethod = true;
                    }
                    // if the method is a getter find the corresponding field if there is one
                    final String methodName = method.getName();
                    if (isAnnotatedMethod && methodName.startsWith("get")) {
                        String typeFieldName = StringUtils.uncapitalize(methodName.substring(3));
                        return new ResourceField(typeFieldName, method.invoke(object).toString());
                    }
                }
            }

            if (resourceField == JsonApiResourceField.id) {
                // then try field "id"
                Field field = object.getClass().getDeclaredField("id");
                field.setAccessible(true);
                final Object id = field.get(object);
                if (id == null) {
                    throw new RuntimeException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID);
                }
                return new ResourceField("id", id.toString());
            }

            String jsonApiType = object.getClass().getSimpleName().toLowerCase();
            if (jsonApiConfiguration.isPluralizedTypeRendered()) {
                jsonApiType = English.plural(jsonApiType, 2);
            }
            return new ResourceField(null, jsonApiType);
        } catch (Exception e) {
            throw new RuntimeException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID);
        }
    }

    static void setTypeForObject(Object object, JsonApiResourceField jsonApiTypeKey, String jsonApiTypeValue) {
        final Field[] declaredFields = object.getClass().getDeclaredFields();
        try {
            // first try annotation on fields
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if (jsonApiTypeKey == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))
                            || (jsonApiTypeKey == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName))) {
                        field.set(object, jsonApiTypeValue);
                        return;
                    }
                }
            }

            // first try annotation on methods
            final Method[] declaredMethods = object.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                final Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    boolean isAnnotatedMethod = false;
                    if (jsonApiTypeKey == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))) {
                        isAnnotatedMethod = true;
                    } else if (jsonApiTypeKey == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName)) {
                        isAnnotatedMethod = true;
                    }
                    // if the method is a setter find the corresponding field if there is one
                    final String methodName = method.getName();
                    if (isAnnotatedMethod && methodName.startsWith("set")) {
                        method.invoke(object, jsonApiTypeValue);
                        return;
                    }
                }
            }

            // then try field directly
            if (jsonApiTypeKey == JsonApiResourceField.id) {
                Field field = object.getClass().getDeclaredField(jsonApiTypeKey.name());
                field.setAccessible(true);
                field.set(object, jsonApiTypeValue);
            }
        } catch (Exception e) {
            System.out.println("Cannot set JSON:API " + jsonApiTypeKey + " on object of type " + object.getClass().getSimpleName());
        }
    }
}
