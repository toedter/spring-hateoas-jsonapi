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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.With;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class is used to build a JSON:API presentation model that uses relationships.
 *
 * @author Kai Toedter
 */

@JsonPropertyOrder({"data", "links", "meta"})
@JsonSerialize(using = JsonApiRelationshipSerializer.class)
@Getter
class JsonApiRelationship {
    private Object data;

    @With(AccessLevel.PACKAGE)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Links links;

    @With(AccessLevel.PACKAGE)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, Object> meta;

    @JsonCreator
    JsonApiRelationship(
            @JsonProperty("data") @Nullable Object data,
            @JsonProperty("links") @Nullable Links links,
            @JsonProperty("meta") @Nullable Map<String, Object> meta
    ) {
        this.data = data;
        this.links = links;
        this.meta = meta;
    }

    public JsonApiRelationship addDataObject(final Object object) {
        if (this.data == null) {
            return new JsonApiRelationship(object, this.links, this.meta);
        } else {
            List<Object> dataList = new ArrayList<>();
            if (!(this.data instanceof Collection<?>)) {
                dataList.add(this.data);
            } else {
                @SuppressWarnings("unchecked")
                Collection<Object> collectionData = (Collection<Object>) this.data;
                dataList.addAll(collectionData);
            }
            dataList.add(object);
            return new JsonApiRelationship(dataList, this.links, this.meta);
        }
    }

    public JsonApiRelationship addDataCollection(final Collection<?> collection) {
        if (this.data == null) {
            return new JsonApiRelationship(collection, this.links, this.meta);
        } else {
            List<Object> dataList = new ArrayList<>();
            if (!(this.data instanceof Collection<?>)) {
                dataList.add(this.data);
            } else {
                dataList.addAll((Collection<?>) data);
            }
            dataList.addAll(collection);
            return new JsonApiRelationship(dataList, this.links, this.meta);
        }
    }

    public JsonApiRelationship isAlwaysSerializedWithDataArray() {
        if (this.data == null) {
            return new JsonApiRelationship(Collections.emptyList(), this.links, this.meta);
        } else if (!(this.data instanceof Collection<?>)) {
            return new JsonApiRelationship(Collections.singletonList(this.data), this.links, this.meta);
        }
        return this;
    }

    /**
     * Creates a JSON:API relationship from an entity model
     * It must be possible to extract the JSON:API id of this object,
     * see https://toedter.github.io/spring-hateoas-jsonapi/#_annotations.
     *
     * @param entityModel the base for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(EntityModel<?> entityModel) {
        return JsonApiRelationship.of(entityModel.getContent());
    }

    /**
     * Creates a JSON:API relationship from an object.
     * It must be possible to extract the JSON:API id of this object,
     * see https://toedter.github.io/spring-hateoas-jsonapi/#_annotations.
     *
     * @param object the base for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(Object object) {
        return new JsonApiRelationship(object, null, null);
    }

    /**
     * Creates a JSON:API relationship from a collection of objects.
     * It must be possible to extract the JSON:API id of each element of this collection,
     * see https://toedter.github.io/spring-hateoas-jsonapi/#_annotations.
     *
     * @param collection the base for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(Collection<?> collection) {
        return new JsonApiRelationship(collection, null, null);
    }

    /**
     * Creates a JSON:API relationship from links
     *
     * @param links the links for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(Links links) {
        return new JsonApiRelationship(null, links, null);
    }

    /**
     * Creates a JSON:API relationship from meta
     *
     * @param meta the meta information for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(Map<String, Object> meta) {
        return new JsonApiRelationship(null, null, meta);
    }

    /**
     * Validates against the JSON:API 1.0 specification.
     *
     * @return true, if the jsonApiRelationship is valid
     */
    @JsonIgnore
    public boolean isValid() {

        if (data == null && links == null && meta == null) {
            return false;
        }

        if (data != null) {
            JsonApiConfiguration jsonApiConfiguration = new JsonApiConfiguration();
            try {
                if (data instanceof Collection<?>) {
                    for (Object jsonApiResource : ((Collection<?>) data)) {
                        toJsonApiResource(jsonApiResource, jsonApiConfiguration);
                    }
                } else {
                    toJsonApiResource(data, jsonApiConfiguration);
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (links != null) {
            final Optional<Link> selfLink = links.getLink("self");
            final Optional<Link> relatedLink = links.getLink("related");
            return selfLink.isPresent() || relatedLink.isPresent();
        }

        // we allow null meta and non-null but empty meta
        // so nothing to check related to meta
        return true;
    }

    static JsonApiResource toJsonApiResource(Object data, JsonApiConfiguration jsonApiConfiguration) {
        // JsonApiResource.getId and getType will throw IllegalStateExceptions
        // if id or type cannot be retrieved.
        Object id = JsonApiResource.getId(data, jsonApiConfiguration).value;
        String type = JsonApiResource.getType(data, jsonApiConfiguration).value;
        return new JsonApiResource(id, type);
    }

    static List<JsonApiResource> toJsonApiResourceCollection(
            Collection<?> collection, JsonApiConfiguration jsonApiConfiguration) {
        List<JsonApiResource> dataList = new ArrayList<>();
        for (Object object : collection) {
            dataList.add(toJsonApiResource(object, jsonApiConfiguration));
        }
        return dataList;
    }
}
