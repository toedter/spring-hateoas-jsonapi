/*
 * Copyright 2025 the original author or authors.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.With;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.util.Assert;

/**
 * This class is used to build a JSON:API presentation model that uses relationships.
 *
 * @author Kai Toedter
 */
@JsonPropertyOrder({"data", "links", "meta"})
@Getter
class JsonApiRelationship {

  @With(AccessLevel.PACKAGE)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Nullable
  private final Object data;

  @With(AccessLevel.PACKAGE)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Nullable
  private final Links links;

  @With(AccessLevel.PACKAGE)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  @Nullable
  private final Map<String, Object> meta;

  @JsonIgnore private Map<Object, Map<String, Object>> metaForResourceIdentifiers;

  @JsonIgnore private final boolean dataExplicitlySet;

  JsonApiRelationship(
      @Nullable Object data,
      @Nullable Links links,
      @Nullable Map<String, Object> meta,
      @Nullable Map<Object, Map<String, Object>> metaForResourceIdentifiers) {
    this(data, links, meta, metaForResourceIdentifiers, true);
  }

  JsonApiRelationship(
      @Nullable Object data,
      @Nullable Links links,
      @Nullable Map<String, Object> meta,
      @Nullable Map<Object, Map<String, Object>> metaForResourceIdentifiers,
      boolean dataExplicitlySet) {
    this.data = data;
    this.links = links;
    this.meta = meta;
    this.metaForResourceIdentifiers = metaForResourceIdentifiers;
    this.dataExplicitlySet = dataExplicitlySet;
  }

  @JsonCreator
  JsonApiRelationship(
      @JsonProperty("data") @Nullable Object data,
      @JsonProperty("links") @Nullable Links links,
      @JsonProperty("meta") @Nullable Map<String, Object> meta) {
    this(data, links, meta, null, true);
  }

  public JsonApiRelationship addDataObject(@Nullable final Object object) {
    return this.addDataObject(object, null);
  }

  public JsonApiRelationship addDataObject(
      @Nullable final Object object, @Nullable Map<String, Object> metaForResourceIdentifier) {
    if (metaForResourceIdentifier != null && !metaForResourceIdentifier.isEmpty()) {
      if (metaForResourceIdentifiers == null) {
        metaForResourceIdentifiers = new HashMap<>();
      }
      metaForResourceIdentifiers.put(object, metaForResourceIdentifier);
    }

    if (this.data == null) {
      return new JsonApiRelationship(
          object, this.links, this.meta, this.metaForResourceIdentifiers, true);
    } else {
      List<Object> dataList = new ArrayList<>();
      if (!(this.data instanceof Collection<?> collection)) {
        dataList.add(this.data);
      } else {
        dataList.addAll(collection);
      }
      dataList.add(object);
      return new JsonApiRelationship(
          dataList, this.links, this.meta, this.metaForResourceIdentifiers, true);
    }
  }

  public JsonApiRelationship addDataCollection(final Collection<?> collection) {
    if (this.data == null) {
      return new JsonApiRelationship(
          collection, this.links, this.meta, this.metaForResourceIdentifiers, true);
    } else {
      List<Object> dataList = new ArrayList<>();
      if (!(this.data instanceof Collection<?>)) {
        dataList.add(this.data);
      } else {
        dataList.addAll((Collection<?>) data);
      }
      dataList.addAll(collection);
      return new JsonApiRelationship(
          dataList, this.links, this.meta, this.metaForResourceIdentifiers, true);
    }
  }

  public JsonApiRelationship isAlwaysSerializedWithDataArray() {
    if (this.data == null) {
      return new JsonApiRelationship(
          Collections.emptyList(), this.links, this.meta, this.metaForResourceIdentifiers, true);
    } else if (!(this.data instanceof Collection<?>)) {
      return new JsonApiRelationship(
          Collections.singletonList(this.data),
          this.links,
          this.meta,
          this.metaForResourceIdentifiers,
          true);
    }
    return this;
  }

  /**
   * Creates a relationship with explicit null data. This will be serialized as "data": null in
   * JSON:API format, representing an empty to-one relationship.
   *
   * @return a new JsonApiRelationship with null data
   */
  public JsonApiRelationship withNullData() {
    return new JsonApiRelationship(
        null, this.links, this.meta, this.metaForResourceIdentifiers, true);
  }

  /**
   * Creates a relationship with explicit empty array data. This will be serialized as "data": [] in
   * JSON:API format, representing an empty to-many relationship.
   *
   * @return a new JsonApiRelationship with empty array data
   */
  public JsonApiRelationship withEmptyData() {
    return new JsonApiRelationship(
        Collections.emptyList(), this.links, this.meta, this.metaForResourceIdentifiers, true);
  }

  public static JsonApiRelationship of(EntityModel<?> entityModel) {
    Object content = entityModel.getContent();
    Assert.notNull(
        content, "Cannot create JSON:API relationship of EntityModel with null content.");
    return JsonApiRelationship.of(content);
  }

  public static JsonApiRelationship of(Object object) {
    return new JsonApiRelationship(object, null, null);
  }

  public static JsonApiRelationship of(
      @Nullable Object object, @Nullable Map<String, Object> resourceIdentifierMeta) {
    JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
    jsonApiRelationship = jsonApiRelationship.addDataObject(object, resourceIdentifierMeta);
    return jsonApiRelationship;
  }

  public static JsonApiRelationship of(Collection<?> collection) {
    return new JsonApiRelationship(collection, null, null);
  }

  public static JsonApiRelationship of(Links links) {
    return new JsonApiRelationship(null, links, null, null, false);
  }

  public static JsonApiRelationship of(Map<String, Object> meta) {
    return new JsonApiRelationship(null, null, meta, null, false);
  }

  @JsonIgnore
  public boolean isValid() {
    if (data == null && links == null && meta == null) {
      return true;
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

  JsonApiResourceIdentifier toJsonApiResource(
      Object data, JsonApiConfiguration jsonApiConfiguration) {
    Map<String, Object> localMeta = null;
    if (metaForResourceIdentifiers != null) {
      localMeta = metaForResourceIdentifiers.get(data);
    }

    // JsonApiResource.getId and getType will throw IllegalStateExceptions
    // if id or type cannot be retrieved.
    String id = JsonApiResourceIdentifier.getId(data, jsonApiConfiguration).value;
    String type = JsonApiResourceIdentifier.getType(data, jsonApiConfiguration).value;
    return new JsonApiResourceIdentifier(id, type, localMeta);
  }

  List<JsonApiResourceIdentifier> toJsonApiResourceCollection(
      Collection<?> collection, JsonApiConfiguration jsonApiConfiguration) {
    List<JsonApiResourceIdentifier> dataList = new ArrayList<>();

    // don't add duplicated with same json:api id and type
    HashMap<String, JsonApiResourceIdentifier> values = new HashMap<>();
    for (Object object : collection) {
      JsonApiResourceIdentifier resourceIdentifier =
          toJsonApiResource(object, jsonApiConfiguration);
      if (values.get(resourceIdentifier.getId() + "." + resourceIdentifier.getType()) == null) {
        dataList.add(resourceIdentifier);
        values.put(
            resourceIdentifier.getId() + "." + resourceIdentifier.getType(), resourceIdentifier);
      }
    }
    return dataList;
  }
}
