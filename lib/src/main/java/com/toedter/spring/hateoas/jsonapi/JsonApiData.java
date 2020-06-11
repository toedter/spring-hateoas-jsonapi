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
import org.springframework.hateoas.*;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.*;

@Value
@Getter(onMethod = @__(@JsonProperty))
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonApiData {
    String id;
    String type;
    Map<String, Object> attributes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Object relationships;
    Links links;

    @JsonCreator
    public JsonApiData(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("attributes") Map<String, Object> attributes,
            @JsonProperty("relationships") Object relationships,
            @JsonProperty("links") Links links
    ) {
        this.id = id;
        this.type = type;
        this.attributes = attributes;
        this.relationships = relationships;
        this.links = links;
    }

    public JsonApiData() {
        this(null, null, null, null, null);
    }

    static List<JsonApiData> extractCollectionContent(@Nullable RepresentationModel<?> representationModel) {

        List<JsonApiData> dataList = new ArrayList<>();

        if (representationModel instanceof JsonApiModel) {
            RepresentationModel<?> content = ((JsonApiModel) representationModel).getContent();
            if (content instanceof CollectionModel) {
                representationModel = content;
            }
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
        Links links = null;
        Object relationships = null;

        if (content instanceof RepresentationModel<?>) {
            links = ((RepresentationModel<?>) content).getLinks();
        }

        if (content instanceof JsonApiModel) {
            JsonApiModel jsonApiRepresentationModel = (JsonApiModel) content;
            relationships = jsonApiRepresentationModel.getRelationships();
            content = jsonApiRepresentationModel.getContent();
        }

        if (content instanceof EntityModel) {
            content = ((EntityModel<?>) content).getContent();
        }

        if (content == null) {
            return Optional.empty();
        }
        JsonApiResource.ResourceField idField;
        try {
            idField = JsonApiResource.getId(content);
        } catch (Exception e) {
            // will lead to "data":[], which is ok with the spec
            final Field[] fields = content.getClass().getDeclaredFields();
            if (fields.length == 0
                    || (links != null && fields.length == 1)
                    || (links != null && fields.length == 2
                    && ("$jacocoData".equals(fields[0].getName()) || "$jacocoData".equals(fields[1].getName())))) {
                return Optional.empty();
            }
            // we have fields, but no id field
            throw e;
        }

        if (isSingleEntity || (links != null && links.isEmpty())) {
            links = null;
        }
        JsonApiResource.ResourceField typeField = JsonApiResource.getType(content);

        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> attributeMap = mapper.convertValue(content, Map.class);
        attributeMap.remove("links");
        attributeMap.remove(idField.name);
        attributeMap.remove(typeField.name);

        final Map<String, Object> finalContentObject = attributeMap;
        Links finalLinks = links;
        String finalId = idField.value;
        String finalType = typeField.value;
        Object finalRelationships = relationships;
        return Optional.of(content)
                .filter(it -> !RESOURCE_TYPES.contains(it.getClass()))
                .map(it -> new JsonApiData()
                        .withId(finalId)
                        .withType(finalType)
                        .withAttributes(finalContentObject)
                        .withRelationships(finalRelationships)
                        .withLinks(finalLinks));
    }

    private static final HashSet<Class<?>> RESOURCE_TYPES = new HashSet<>(
            Arrays.asList(RepresentationModel.class, EntityModel.class, CollectionModel.class, PagedModel.class));
}
