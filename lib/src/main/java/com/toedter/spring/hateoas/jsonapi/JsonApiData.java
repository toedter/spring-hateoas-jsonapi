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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.atteo.evo.inflector.English;
import org.springframework.hateoas.*;
import org.springframework.lang.Nullable;

import java.util.*;

@Value
@Getter(onMethod = @__(@JsonProperty))
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonApiData {
    Object id;
    String type;
    @JsonIgnoreProperties(value = {"id", "type"})
    Object attributes;
    Links links;

    @JsonCreator
    public JsonApiData(
            @JsonProperty("id") Object id,
            @JsonProperty("type") String type,
            @JsonProperty("attributes") Object attributes,
            @JsonProperty("links") Links links
    ) {
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
                Optional<JsonApiData> jsonApiData = extractContent(entity, false);
                jsonApiData.ifPresent(dataList::add);
            }
        } else {
            Optional<JsonApiData> jsonApiData = extractContent(representationModel, true);
            jsonApiData.ifPresent(dataList::add);
        }
        return dataList;
    }

    static Optional<JsonApiData> extractContent(@Nullable Object content, boolean isSingleEntity) {
        Object contentObject = content;
        Links links = null;
        if (!isSingleEntity && content instanceof RepresentationModel<?>) {
            links = ((RepresentationModel<?>) content).getLinks();
        }
        if (content instanceof EntityModel) {
            content = ((EntityModel<?>) content).getContent();
        }
        if (links != null && links.isEmpty()) {
            links = null;
        }
        ObjectMapper mapper = new ObjectMapper();

        @SuppressWarnings("unchecked")
        Map<String, Object> attributeMap = mapper.convertValue(contentObject, Map.class);

        // if the content is a RepresentationModel itself,
        // we have to convert it, otherwise and infinite recursion would happen,
        // since this is part of the serialization of a RepresentationModel
        if (contentObject instanceof RepresentationModel<?>) {
            attributeMap.remove("links");
            contentObject = attributeMap;
        }
        Object finalContentObject = contentObject;
        Links finalLinks = links;
        return Optional.ofNullable(content)
                .filter(it -> !RESOURCE_TYPES.contains(it.getClass()))
                .map(it -> new JsonApiData()
                        .withId(getId(attributeMap))
                        .withType(getType(attributeMap, it))
                        .withAttributes(finalContentObject)
                        .withLinks(finalLinks));
    }

    static Object getId(Map<String, Object> attributeMap) {
        Object id = attributeMap.get("id");
        if (id == null) {
            throw new RuntimeException("JSON:API resource must have property \"id\".");
        }
        return id;
    }

    static String getType(Map<String, Object> attributeMap, Object content) {
        Object type = attributeMap.get("type");
        if (type == null) {
            String singleType = content.getClass().getSimpleName().toLowerCase();
            return English.plural(singleType, 2);
        }
        return type.toString();
    }

    private static final HashSet<Class<?>> RESOURCE_TYPES = new HashSet<>(
            Arrays.asList(RepresentationModel.class, EntityModel.class, CollectionModel.class, PagedModel.class));
}
