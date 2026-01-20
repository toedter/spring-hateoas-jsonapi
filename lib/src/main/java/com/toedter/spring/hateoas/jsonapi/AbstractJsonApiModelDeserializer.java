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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.util.Assert;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

@Slf4j
abstract class AbstractJsonApiModelDeserializer<T> extends StdDeserializer<T> {

  protected final JsonMapper jsonMapper;
  protected final JavaType contentType;
  protected final JsonApiConfiguration jsonApiConfiguration;

  AbstractJsonApiModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
    this(
        TypeFactory.createDefaultInstance()
            .constructSimpleType(JsonApiDocument.class, new JavaType[0]),
        jsonApiConfiguration);
  }

  protected AbstractJsonApiModelDeserializer(
      JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
    super(contentType);
    this.contentType = contentType;
    this.jsonApiConfiguration = jsonApiConfiguration;
    this.jsonMapper = jsonApiConfiguration.getJsonMapper();
  }

  @Override
  @SuppressWarnings("unchecked")
  public T deserialize(JsonParser p, DeserializationContext ctxt) {
    boolean isEntityModelCollection = isEntityModelCollection();

    JsonApiDocument doc = p.readValueAs(JsonApiDocument.class);
    Object data = doc.getData();

    if (data instanceof Collection<?> collection) {
      Assert.notNull(collection, "JsonApiDocument data must not be null!");
      List<Object> resources = deserializeCollection(collection, isEntityModelCollection, doc);
      return convertToRepresentationModel(resources, doc);
    }

    if (data instanceof HashMap<?, ?>) {
      HashMap<String, Object> typedData = (HashMap<String, Object>) data;
      final Object objectFromProperties = convertToResource(typedData, false, doc, null, false);
      if (objectFromProperties != null) {
        return convertToRepresentationModel(Collections.singletonList(objectFromProperties), doc);
      }
    }

    return convertToRepresentationModel(Collections.emptyList(), doc);
  }

  private boolean isEntityModelCollection() {
    if (this instanceof JsonApiPagedModelDeserializer
        || this instanceof JsonApiCollectionModelDeserializer) {
      JavaType javaType = contentType.containedType(0);
      return javaType.getRawClass() == EntityModel.class;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private List<Object> deserializeCollection(
      Collection<?> collection, boolean isEntityModelCollection, JsonApiDocument doc) {
    return collection.stream()
        .filter(HashMap.class::isInstance)
        .map(data -> (HashMap<String, Object>) data)
        .map(data -> convertToResource(data, isEntityModelCollection, doc, null, false))
        .filter(java.util.Objects::nonNull)
        .toList();
  }

  @Override
  public ValueDeserializer<?> createContextual(
      DeserializationContext ctxt, @Nullable BeanProperty property) {
    JavaType type =
        property == null ? ctxt.getContextualType() : property.getType().getContentType();
    return createJsonDeserializer(type);
  }

  @Nullable
  @SuppressWarnings("unchecked")
  protected Object convertToResource(
      @Nullable HashMap<String, Object> data,
      boolean wrapInEntityModel,
      @Nullable JsonApiDocument doc,
      @Nullable JavaType javaType,
      boolean useDataForCreation) {
    if (data == null) {
      return null;
    }

    Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
    JavaType rootType = determineRootType(data, javaType);
    Object objectFromProperties =
        createObjectFromData(attributes, data, rootType, useDataForCreation);

    setResourceIdentifierFields(objectFromProperties, data);

    if (wrapInEntityModel) {
      return wrapInEntityModel(objectFromProperties, data, doc);
    }

    return objectFromProperties;
  }

  private JavaType determineRootType(
      @Nullable HashMap<String, Object> data, @Nullable JavaType javaType) {
    JavaType rootType = javaType != null ? javaType : JacksonHelper.findRootType(this.contentType);

    if (jsonApiConfiguration.isTypeForClassUsedForDeserialization() && data != null) {
      String jsonApiType = (String) data.get("type");
      if (jsonApiType != null) {
        Class<?> clazz = jsonApiConfiguration.getClassForType(jsonApiType);
        if (clazz != null) {
          validateTypeAssignability(clazz, rootType);
          rootType = jsonMapper.constructType(clazz);
        }
      }
    }

    return rootType;
  }

  private void validateTypeAssignability(Class<?> clazz, JavaType rootType) {
    if (!rootType.getRawClass().isAssignableFrom(clazz)) {
      throw new IllegalArgumentException(clazz + " is not assignable to " + rootType.getRawClass());
    }
  }

  private Object createObjectFromData(
      @Nullable Map<String, Object> attributes,
      HashMap<String, Object> data,
      JavaType rootType,
      boolean useDataForCreation) {
    if (attributes != null) {
      // use the json mapper to support custom deserializers (e.g., JMoleculesModule)
      return jsonMapper.convertValue(attributes, rootType);
    }

    if (useDataForCreation) {
      // we have to use the "real" json mapper due to polymorphic deserialization using Jackson
      return jsonMapper.convertValue(data, rootType);
    }

    return createDefaultInstance(rootType);
  }

  private Object createDefaultInstance(JavaType rootType) {
    try {
      Class<?> clazz = rootType.getRawClass();
      return clazz.getDeclaredConstructor().newInstance();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot convert data to resource.", e);
    }
  }

  private void setResourceIdentifierFields(
      Object objectFromProperties, HashMap<String, Object> data) {
    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
        objectFromProperties,
        JsonApiResourceIdentifier.JsonApiResourceField.ID,
        (String) data.get("id"),
        jsonMapper);
    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
        objectFromProperties,
        JsonApiResourceIdentifier.JsonApiResourceField.TYPE,
        (String) data.get("type"),
        jsonMapper);
  }

  private Object wrapInEntityModel(
      Object objectFromProperties, HashMap<String, Object> data, @Nullable JsonApiDocument doc) {
    Links links = extractLinks(data);
    JsonApiEntityModelDeserializer jsonApiEntityModelDeserializer =
        new JsonApiEntityModelDeserializer(jsonApiConfiguration);
    JsonApiDocument jsonApiDocument =
        new JsonApiDocument(null, data, null, null, links, doc != null ? doc.getIncluded() : null);
    return jsonApiEntityModelDeserializer.convertToRepresentationModel(
        Collections.singletonList(objectFromProperties), jsonApiDocument);
  }

  private Links extractLinks(HashMap<String, Object> data) {
    Object linksData = data.get("links");
    if (linksData instanceof Map<?, ?> map) {
      // Use JsonApiLinksDeserializer to properly deserialize links
      JsonApiLinksDeserializer linksDeserializer = new JsonApiLinksDeserializer();
      @SuppressWarnings("unchecked")
      Map<String, Object> linksMap = (Map<String, Object>) map;
      return linksDeserializer.deserialize(linksMap);
    }
    return Links.NONE;
  }

  protected abstract T convertToRepresentationModel(List<Object> resources, JsonApiDocument doc);

  protected abstract ValueDeserializer<?> createJsonDeserializer(JavaType type);
}
