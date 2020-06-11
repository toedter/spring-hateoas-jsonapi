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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Value
@Getter(onMethod = @__(@JsonProperty))
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonApiResource {
    public static final String JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID =
            "JSON:API resource object must have property \"id\".";

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

    static class IdField {
        String name;
        String value;

        public IdField(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    static IdField getId(Object object) {
        try {
            // first search for Annotation @Id
            final Field[] declaredFields = object.getClass().getDeclaredFields();
            for (Field field : declaredFields) {
                final Annotation[] annotations = field.getAnnotations();
                for (Annotation annotation : annotations) {
                    final String annotationName = annotation.annotationType().getCanonicalName();
                    if ("javax.persistence.Id".equals(annotationName)
                            || "com.toedter.spring.hateoas.jsonapi.JsonApiId".equals(annotationName)) {
                        field.setAccessible(true);
                        return new IdField(field.getName(), field.get(object).toString());
                    }
                }
            }

            // then try field "id"
            Field field = object.getClass().getDeclaredField("id");
            field.setAccessible(true);
            final Object id = field.get(object);
            if (id == null) {
                throw new RuntimeException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID);
            }
            return new IdField("id", id.toString());
        } catch (Exception e) {
            throw new RuntimeException(JSON_API_RESOURCE_OBJECT_MUST_HAVE_PROPERTY_ID);
        }
    }

    static String getType(Object object) {
        try {
            Field field = object.getClass().getDeclaredField("_type");
            field.setAccessible(true);
            return field.get(object).toString();
        } catch (Exception e) {
            // pluralize class name
            String singleType = object.getClass().getSimpleName().toLowerCase();
            return English.plural(singleType, 2);
        }
    }
}
