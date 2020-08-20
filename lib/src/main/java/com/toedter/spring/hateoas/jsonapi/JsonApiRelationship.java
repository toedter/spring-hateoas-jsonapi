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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import java.lang.reflect.Array;
import java.util.*;

/**
 * This class is used to build a JSON:API presentation model that uses relationships.
 *
 * @author Kai Toedter
 */
@Value
@JsonPropertyOrder({"data", "links", "meta"})
class JsonApiRelationship {
    final static JsonApiConfiguration jsonApiConfiguration = new JsonApiConfiguration();

    @Getter
    Object data;

    @Getter
    @With(AccessLevel.PACKAGE)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Links links;

    @Getter
    @With(AccessLevel.PACKAGE)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    Map<String, Object> meta;

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

    public JsonApiRelationship withData(JsonApiResource jsonApiResource) {
        if (this.data == null) {
            return new JsonApiRelationship(jsonApiResource, this.links, this.meta);
        } else {
            List<JsonApiResource> dataList = new ArrayList<>();
            if (this.data instanceof JsonApiResource) {
                dataList.add((JsonApiResource) this.data);
            } else {
                @SuppressWarnings("unchecked")
                Collection<JsonApiResource> collectionData = (Collection<JsonApiResource>) this.data;
                dataList.addAll(collectionData);
            }
            dataList.add(jsonApiResource);
            return new JsonApiRelationship(dataList, this.links, this.meta);
        }
    }

    /**
     * Creates a JSON:API relationship from an entity model
     *
     * @param entityModel the base for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(EntityModel<?> entityModel) {
        return JsonApiRelationship.of(entityModel.getContent());
    }

    /**
     * Creates a JSON:API relationship from an object
     *
     * @param object the base for the relationship
     * @return the JSON:API relationship
     */
    public static JsonApiRelationship of(Object object) {
        Object id = JsonApiResource.getId(object, jsonApiConfiguration).value;
        String type = JsonApiResource.getType(object, jsonApiConfiguration).value;
        return new JsonApiRelationship(new JsonApiResource(id, type), null, null);
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
     * Validates the given jsonApiRelationship against the JSON:API 1.0 specification.
     *
     * @param jsonApiRelationship the JSON:API relationship to be validated
     * @return true, if the jsonApiRelationship is valid
     */
    public static boolean isValid(JsonApiRelationship jsonApiRelationship) {

        final Object data = jsonApiRelationship.data;
        final Links links = jsonApiRelationship.links;
        final Map<String, Object> meta = jsonApiRelationship.meta;

        if (data == null && links == null && meta == null) {
            return false;
        }

        if (data != null) {
            try {
                if (data instanceof Collection<?>) {
                    for (Object jsonApiResource : ((Collection<?>) data)) {
                        validateJsonApiResource(jsonApiResource);
                    }
                } else {
                    validateJsonApiResource(data);
                }
            } catch (Exception e) {
                return false;
            }
        }

        if (links != null) {
            final Optional<Link> selfLink = links.getLink("self");
            final Optional<Link> relatedLink = links.getLink("related");
            if (!selfLink.isPresent() && !relatedLink.isPresent()) {
                return false;
            }
        }

        // we allow null meta and non-null but empty meta
        // so nothing to check related to meta
        return true;
    }

    private static void validateJsonApiResource(Object data) {
        // sonApiResource.getId and getType will throw IllegalStateExceptions
        // if id or type cannot be retrieved.
        Object id = JsonApiResource.getId(data, jsonApiConfiguration).value;
        String type = JsonApiResource.getType(data, jsonApiConfiguration).value;
    }
}
