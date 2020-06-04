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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.springframework.hateoas.*;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@Value
@Getter(onMethod = @__(@JsonProperty))
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonApiData {
    Object id;
    String type;
    Map<String, Object> attributes;
    Links links;

    @JsonCreator
    public JsonApiData(
            @JsonProperty("id") Object id,
            @JsonProperty("type") String type,
            @JsonProperty("attributes") Map<String, Object> attributes,
            @JsonProperty("links") Links links) {
        this.id = id;
        this.type = type;
        this.attributes = attributes;
        this.links = links;
    }

    public JsonApiData() {
        this(null, null, null, null);
    }

    static List<JsonApiData> extractCollectionContent(@Nullable RepresentationModel<?> representationModel) {
        List<JsonApiData> dataList = new ArrayList<>();

        if (representationModel == null) {
            return dataList;
        }

        if (representationModel instanceof CollectionModel) {
            for (Object entity : ((CollectionModel<?>) representationModel).getContent()) {
                Optional<JsonApiData> jsonApiData = extractContent(entity);
                jsonApiData.ifPresent(dataList::add);
            }
        } else if (representationModel instanceof EntityModel) {
            Optional<JsonApiData> jsonApiData = extractContent(((EntityModel<?>) representationModel).getContent());
            jsonApiData.ifPresent(dataList::add);
        }
        return dataList;
    }

    static Optional<JsonApiData> extractContent(@Nullable Object content) {
        return Optional.ofNullable(content)
                .filter(it -> !RESOURCE_TYPES.contains(it.getClass()))
                .map(it -> new JsonApiData()
                        .withId(invokeGetter(it, "id"))
                        .withType(StringUtils.uncapitalize(it.getClass().getSimpleName()))
                        .withAttributes(extractAttributes(it)));
    }

    private static Map<String, Object> extractAttributes(Object object) {
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                String name = field.getName();
                if (!"type".equals(name) && !"id".equals(name)) {
                    map.put(name, invokeGetter(object, name));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    private static Object invokeGetter(Object obj, String variableName) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(variableName, obj.getClass());
            Method getter = propertyDescriptor.getReadMethod();
            return getter.invoke(obj);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final HashSet<Class<?>> RESOURCE_TYPES = new HashSet<>(
            Arrays.asList(RepresentationModel.class, EntityModel.class, CollectionModel.class, PagedModel.class));
}
