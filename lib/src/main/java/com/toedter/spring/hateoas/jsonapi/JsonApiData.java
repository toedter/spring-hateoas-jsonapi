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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.lang.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.toedter.spring.hateoas.jsonapi.ReflectionUtils.getAllDeclaredFields;

@Value
@Getter(onMethod_ = {@JsonProperty})
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
class JsonApiData {
    private String id;
    private String type;
    private Map<String, Object> attributes;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Object relationships;
    private Links links;

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

    static List<JsonApiData> extractCollectionContent(
            @Nullable CollectionModel<?> collectionModel,
            JsonApiConfiguration jsonApiConfiguration,
            @Nullable HashMap<String, Collection<String>> sparseFieldsets) {

        List<JsonApiData> dataList = new ArrayList<>();
        if (collectionModel != null) {
            for (Object entity : collectionModel.getContent()) {
                Optional<JsonApiData> jsonApiData =
                        extractContent(entity, false, jsonApiConfiguration, sparseFieldsets);
                jsonApiData.ifPresent(dataList::add);
            }
        }
        return dataList;
    }

    static Optional<JsonApiData> extractContent(
            @Nullable Object content,
            boolean isSingleEntity,
            JsonApiConfiguration jsonApiConfiguration,
            @Nullable HashMap<String, Collection<String>> sparseFieldsets) {

        Links links = null;
        Object relationships = null;

        if (content instanceof RepresentationModel<?>) {
            links = ((RepresentationModel<?>) content).getLinks();
        }

        if (content instanceof JsonApiModel) {
            JsonApiModel jsonApiRepresentationModel = (JsonApiModel) content;
            relationships = jsonApiRepresentationModel.getRelationships();
            sparseFieldsets = jsonApiRepresentationModel.getSparseFieldsets();
            content = jsonApiRepresentationModel.getContent();
        }

        if (content instanceof EntityModel) {
            content = ((EntityModel<?>) content).getContent();
        }

        if (content == null) {
            // will lead to "data":null, which is compliant with the JSON:API spec
            return Optional.empty();
        }

        final Field[] fields = getAllDeclaredFields(content.getClass());
        if (fields.length == 0
                || (content instanceof RepresentationModel<?> && fields.length == 1)
                || (content instanceof RepresentationModel<?> && fields.length == 2
                && ("$jacocoData".equals(fields[0].getName()) || "$jacocoData".equals(fields[1].getName())))) {
            return Optional.empty();
        }

        JsonApiResource.ResourceField idField;
        idField = JsonApiResource.getId(content, jsonApiConfiguration);

        if (isSingleEntity || links != null && links.isEmpty()) {
            links = null;
        }
        JsonApiResource.ResourceField typeField = JsonApiResource.getType(content, jsonApiConfiguration);

        Map<String, Object> attributeMap = PropertyUtils.extractPropertyValues(content);
        attributeMap.remove("links");
        attributeMap.remove(idField.name);
        attributeMap.remove(typeField.name);

        Links finalLinks = links;
        String finalId = idField.value;
        String finalType = typeField.value;
        Object finalRelationships = relationships;

        // apply sparse fieldsets
        if (sparseFieldsets != null) {
            Collection<String> attributes = sparseFieldsets.get(finalType);
            if (attributes != null) {
                Set<String> keys = new HashSet<>(attributeMap.keySet());
                for (String key : keys) {
                    if (!attributes.contains(key)) {
                        attributeMap.remove(key);
                    }
                }
            }
        }
        final Map<String, Object> finalContentObject = attributeMap;

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
