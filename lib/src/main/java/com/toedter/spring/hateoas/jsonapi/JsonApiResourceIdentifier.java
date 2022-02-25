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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.atteo.evo.inflector.English;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;

import static com.toedter.spring.hateoas.jsonapi.ReflectionUtils.getAllDeclaredFields;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getAllDeclaredMethods;


@Value
@Getter(onMethod_ = {@JsonProperty})
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonApiResourceIdentifier {
    public static final String JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID =
            "JSON:API resource object must have property \"id\".";
    public static final String JSONAPI_ID_ANNOTATION = "com.toedter.spring.hateoas.jsonapi.JsonApiId";
    public static final String JSONAPI_TYPE_ANNOTATION = "com.toedter.spring.hateoas.jsonapi.JsonApiType";
    public static final String JPA_ID_ANNOTATION = "javax.persistence.Id";
    public static final String ID = "id";
    public static final String TYPE = "type";

    Object id;
    String type;
    Map<String, Object> meta;

    @JsonCreator
    public JsonApiResourceIdentifier(
            @JsonProperty(ID) Object id,
            @JsonProperty(TYPE) String type,
            @Nullable Map<String, Object> meta
    ) {
        this.id = id;
        this.type = type;
        this.meta = meta;
    }

    @JsonCreator
    public JsonApiResourceIdentifier(
            @JsonProperty(ID) Object id,
            @JsonProperty(TYPE) String type
    ) {
        this(id, type, null);
    }

    static class ResourceField {
        final String name;
        final String value;

        public ResourceField(String name, String value) {
            this.name = name;
            this.value = value;
        }
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
            // check Class based JSON:API type annotation
            if (resourceField == JsonApiResourceField.type
                    && object.getClass().isAnnotationPresent(JsonApiTypeForClass.class)) {
                JsonApiTypeForClass annotation = object.getClass().getAnnotation(JsonApiTypeForClass.class);
                return new ResourceField(TYPE, annotation.value());
            }

            // then search for field annotation
            final Field[] declaredFields = getAllDeclaredFields(object.getClass());
            Field jpaIdField = null;
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if (resourceField == JsonApiResourceField.id) {
                        if (JPA_ID_ANNOTATION.equals(annotationName)) {
                            jpaIdField = field;
                        }
                        if (JSONAPI_ID_ANNOTATION.equals(annotationName)) {
                            return new ResourceField(field.getName(), field.get(object).toString());
                        }
                    } else if (JSONAPI_TYPE_ANNOTATION.equals(annotationName)) {
                        return new ResourceField(field.getName(), field.get(object).toString());
                    }
                }
            }

            // then search for method annotation
            final Method[] declaredMethods = getAllDeclaredMethods(object.getClass());
            Method jpaIdMethod = null;
            for (Method method : declaredMethods) {
                final Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if (resourceField == JsonApiResourceField.id) {
                        if (JPA_ID_ANNOTATION.equals(annotationName)) {
                            jpaIdMethod = method;
                        }
                        if (JSONAPI_ID_ANNOTATION.equals(annotationName) && method.getReturnType() != void.class) {
                            return getResourceFieldForMethod(object, method, resourceField);
                        }
                    } else if (JSONAPI_TYPE_ANNOTATION.equals(annotationName) && method.getReturnType() != void.class) {
                        return getResourceFieldForMethod(object, method, resourceField);
                    }
                }
            }

            // JPA @id annotations have lower priority than @JsonApiId annotations,
            // this is why they are returned later in the game.
            if (jpaIdField != null) {
                return new ResourceField(jpaIdField.getName(), jpaIdField.get(object).toString());
            }

            if (jpaIdMethod != null) {
                return getResourceFieldForMethod(object, jpaIdMethod, resourceField);
            }

            if (resourceField == JsonApiResourceField.id) {
                // then try field "id"
                Field field = ReflectionUtils.findField(object.getClass(), ID);
                //noinspection ConstantConditions
                field.setAccessible(true);
                final Object id = field.get(object);
                if (id == null) {
                    throw new IllegalStateException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID);
                }
                return new ResourceField(ID, id.toString());
            }

            String type = jsonApiConfiguration.getTypeForClass(object.getClass());
            if (type != null) {
                return new ResourceField(TYPE, type);
            }

            String jsonApiType = object.getClass().getSimpleName();
            if (jsonApiConfiguration.isLowerCasedTypeRendered()) {
                jsonApiType = jsonApiType.toLowerCase();
            }
            if (jsonApiConfiguration.isPluralizedTypeRendered()) {
                jsonApiType = English.plural(jsonApiType, 2);
            }
            return new ResourceField(TYPE, jsonApiType);
        } catch (Exception e) {
            throw new IllegalStateException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID + "::: " + object);
        }
    }

    private static ResourceField getResourceFieldForMethod(
            Object object, Method jsonApiIdMethod, JsonApiResourceField resourceField)
            throws IllegalAccessException, InvocationTargetException {
        final String methodName = jsonApiIdMethod.getName();
        if (methodName.startsWith("get")) {
            String fieldName = StringUtils.uncapitalize(methodName.substring(3));
            return new ResourceField(fieldName, jsonApiIdMethod.invoke(object).toString());
        }
        return new ResourceField(resourceField.name(), jsonApiIdMethod.invoke(object).toString());
    }

    static void setJsonApiResourceFieldAttributeForObject(Object object, JsonApiResourceField name, String value) {
        final Field[] declaredFields = getAllDeclaredFields(object.getClass());
        try {
            // first try annotation on fields
            for (Field field : declaredFields) {
                field.setAccessible(true);
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if (name == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))
                            || (name == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName))) {
                        setFieldValue(object, value, field);
                        return;
                    }
                }
            }

            // secondly try annotation on methods
            final Method[] declaredMethods = getAllDeclaredMethods(object.getClass());
            for (Method method : declaredMethods) {
                final Annotation[] annotations = method.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    boolean isAnnotatedMethod = false;
                    if (name == JsonApiResourceField.id && (JPA_ID_ANNOTATION.equals(annotationName)
                            || JSONAPI_ID_ANNOTATION.equals(annotationName))) {
                        isAnnotatedMethod = true;
                    } else if (name == JsonApiResourceField.type
                            && JSONAPI_TYPE_ANNOTATION.equals(annotationName)) {
                        isAnnotatedMethod = true;
                    }
                    // if the method is a setter find the corresponding field if there is one,
                    // as heuristic the method should take one parameter
                    if (isAnnotatedMethod && method.getParameterCount() == 1) {
                        if (method.getParameterTypes()[0] == UUID.class) {
                            method.invoke(object, UUID.fromString(value));
                        } else {
                            method.invoke(object, value);
                        }
                        return;
                    }
                }
            }

            // then try field directly
            if (name == JsonApiResourceField.id) {
                Field field = findField(object.getClass(), name.name());
                //noinspection ConstantConditions
                field.setAccessible(true);
                setFieldValue(object, value, field);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Cannot set JSON:API field '" + name +
                    "' on object of type " + object.getClass().getSimpleName());
        }
    }

    private static void setFieldValue(Object object, @Nullable String value, Field field) throws IllegalAccessException {
        Class<?> type = field.getType();

        if (type != String.class && value != null) {
            if (type == Long.TYPE || type == Long.class) {
                field.set(object, Long.parseLong(value));
            } else if (type == Integer.TYPE || type == Integer.class) {
                field.set(object, Integer.parseInt(value));
            } else if (type == UUID.class) {
                field.set(object, UUID.fromString(value));
            }
        } else {
            field.set(object, value);
        }
    }

}
