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

import static com.toedter.spring.hateoas.jsonapi.ReflectionUtils.getAllDeclaredFields;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.With;
import lombok.extern.java.Log;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

@Getter(onMethod_ = {@JsonProperty})
@With(AccessLevel.PACKAGE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Log
@SuppressWarnings("squid:S3011")
class JsonApiData {

  String id;
  String type;
  Map<String, Object> attributes;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  Object relationships;

  Links links;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  Map<String, Object> meta;

  @JsonCreator
  public JsonApiData(
      @JsonProperty("id") @Nullable String id,
      @JsonProperty("type") @Nullable String type,
      @JsonProperty("attributes") @Nullable Map<String, Object> attributes,
      @JsonProperty("relationships") @Nullable Object relationships,
      @JsonProperty("links") @Nullable Links links,
      @JsonProperty("meta") @Nullable Map<String, Object> meta) {
    this.id = id;
    this.type = type;
    this.attributes = attributes;
    this.relationships = relationships;
    this.links = links;
    this.meta = meta;
  }

  private static class JsonApiDataWithoutSerializedAttributes extends JsonApiData {

    JsonApiDataWithoutSerializedAttributes(JsonApiData jsonApiData) {
      super(
          jsonApiData.id,
          jsonApiData.type,
          null,
          jsonApiData.relationships,
          jsonApiData.links,
          jsonApiData.meta);
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public @Nullable Map<String, Object> getAttributes() {
      return null;
    }
  }

  public static List<JsonApiData> extractCollectionContent(
      CollectionModel<?> collectionModel,
      JsonMapper jsonMapper,
      JsonApiConfiguration jsonApiConfiguration,
      @Nullable Map<String, Collection<String>> sparseFieldsets,
      boolean eliminateDuplicates) {
    if (eliminateDuplicates) {
      HashMap<String, JsonApiData> values = new HashMap<>();
      for (Object entity : collectionModel.getContent()) {
        Optional<JsonApiData> jsonApiData =
            extractContent(entity, false, jsonMapper, jsonApiConfiguration, sparseFieldsets);
        jsonApiData.ifPresent(
            apiData -> values.put(apiData.getId() + "." + apiData.getType(), apiData));
      }
      return new ArrayList<>(values.values());
    } else {
      List<JsonApiData> dataList = new ArrayList<>();
      for (Object entity : collectionModel.getContent()) {
        Optional<JsonApiData> jsonApiData =
            extractContent(entity, false, jsonMapper, jsonApiConfiguration, sparseFieldsets);
        jsonApiData.ifPresent(dataList::add);
      }
      return dataList;
    }
  }

  public static Optional<JsonApiData> extractContent(
      @Nullable Object content,
      boolean isSingleEntity,
      JsonMapper jsonMapper,
      JsonApiConfiguration jsonApiConfiguration,
      @Nullable Map<String, Collection<String>> sparseFieldsets) {
    Links links = null;
    Map<String, JsonApiRelationship> relationships = null;
    Map<String, Object> metaData = null;

    if (content instanceof RepresentationModel<?>) {
      links = ((RepresentationModel<?>) content).getLinks();
    }

    if (content instanceof JsonApiModel jsonApiModel) {
      relationships = jsonApiModel.getRelationships();
      sparseFieldsets = jsonApiModel.getSparseFieldsets();
      metaData = jsonApiModel.getMetaData();
      content = jsonApiModel.getContent();
    }

    if (content instanceof EntityModel) {
      content = ((EntityModel<?>) content).getContent();
    }

    if (content == null) {
      // will lead to "data":null, which is compliant with the JSON:API spec
      return Optional.empty();
    }

    // when running with code coverage in IDEs,
    // some additional fields might be introduced.
    // Those should be ignored.
    final Field[] fields = getAllDeclaredFields(content.getClass());
    boolean validFieldFound = false;
    for (Field field : fields) {
      if (!"$jacocoData".equals(field.getName())
          && !"__$lineHits$__".equals(field.getName())
          && (!(content instanceof RepresentationModel && "links".equals(field.getName())))) {
        validFieldFound = true;
        break;
      }
    }
    if (!validFieldFound) {
      return Optional.empty();
    }

    JsonApiResourceIdentifier.ResourceField idField;
    idField = JsonApiResourceIdentifier.getId(content, jsonApiConfiguration);

    // Only clear links if not configured to be at resource level
    if (!jsonApiConfiguration.isLinksAtResourceLevel()
        && (isSingleEntity || (links != null && links.isEmpty()))) {
      links = null;
    }

    // If configured for resource level links but links are empty, set to null
    if (jsonApiConfiguration.isLinksAtResourceLevel() && links != null && links.isEmpty()) {
      links = null;
    }

    // breaking change: JSON:API only allows a self link within resources (not top-level!),
    // see https://jsonapi.org/format/#document-resource-object-links.
    // All other resource links are now removed.
    if (links != null && jsonApiConfiguration.isJsonApiCompliantLinks()) {
      Links validJsonApiLinks = Links.NONE;
      for (Link link : links) {
        if (link.hasRel("self")) {
          validJsonApiLinks = validJsonApiLinks.and(link);
        } else {
          log.warning("removed invalid JSON:API resource-level link: " + link.getRel());
        }
      }
      links = validJsonApiLinks;
    }

    JsonApiResourceIdentifier.ResourceField typeField =
        JsonApiResourceIdentifier.getType(content, jsonApiConfiguration);

    JavaType mapType =
        jsonMapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
    Map<String, Object> attributeMap = jsonMapper.convertValue(content, mapType);

    attributeMap.remove("links");
    attributeMap.remove(idField.name);

    // fix #60
    if (jsonApiConfiguration.getJsonApiIdNotSerializedForValue() != null
        && jsonApiConfiguration.getJsonApiIdNotSerializedForValue().equals(idField.value)) {
      idField = new JsonApiResourceIdentifier.ResourceField(idField.name, null);
    }

    // fix #53
    if (!content.getClass().isAnnotationPresent(JsonApiTypeForClass.class)) {
      attributeMap.remove(typeField.name);
    }

    Links finalLinks = links;
    String finalId = idField.value;
    String finalType = typeField.value;
    Map<String, JsonApiRelationship> finalRelationships = relationships;

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

    // extract annotated meta data
    for (Field field : ReflectionUtils.getAllDeclaredFields(content.getClass())) {
      if (field.getAnnotation(JsonApiMeta.class) != null
          && attributeMap.containsKey(field.getName())) {
        attributeMap.remove(field.getName());
        try {
          field.setAccessible(true);
          if (metaData == null) {
            metaData = new LinkedHashMap<>();
          }
          metaData.put(field.getName(), field.get(content));
        } catch (IllegalAccessException e) {
          throw new IllegalArgumentException(
              "Cannot get JSON:API meta data from annotated property: " + field.getName(), e);
        }
      }
    }

    for (Method method : content.getClass().getMethods()) {
      if (method.getAnnotation(JsonApiMeta.class) != null) {
        try {
          String methodName = method.getName();
          if (methodName.startsWith("get")) {
            methodName = StringUtils.uncapitalize(methodName.substring(3));
          }
          if (attributeMap.containsKey(methodName)) {
            method.setAccessible(true);
            if (metaData == null) {
              metaData = new LinkedHashMap<>();
            }
            if (method.getReturnType() != void.class) {
              attributeMap.remove(methodName);
              metaData.put(methodName, method.invoke(content));
            }
          }
        } catch (Exception e) {
          throw new IllegalArgumentException(
              "Cannot get JSON:API meta data from annotated method: " + method.getName(), e);
        }
      }
    }

    Map<String, Object> finalMetaData = metaData;

    JsonApiData jsonApiData =
        new JsonApiData(
            finalId, finalType, attributeMap, finalRelationships, finalLinks, finalMetaData);

    if (!attributeMap.isEmpty() || jsonApiConfiguration.isEmptyAttributesObjectSerialized()) {
      return Optional.of(content)
          .filter(it -> !RESOURCE_TYPES.contains(it.getClass()))
          .map(it -> jsonApiData);
    } else {
      return Optional.of(content)
          .filter(it -> !RESOURCE_TYPES.contains(it.getClass()))
          .map(it -> new JsonApiDataWithoutSerializedAttributes(jsonApiData));
    }
  }

  static final HashSet<Class<?>> RESOURCE_TYPES =
      new HashSet<>(
          Arrays.asList(
              RepresentationModel.class,
              EntityModel.class,
              CollectionModel.class,
              PagedModel.class));
}
