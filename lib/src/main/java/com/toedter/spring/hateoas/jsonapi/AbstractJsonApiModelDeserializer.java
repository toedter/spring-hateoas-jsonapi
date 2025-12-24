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
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.TypeFactory;

@Slf4j
abstract class AbstractJsonApiModelDeserializer<T> extends StdDeserializer<T> {

  protected final JsonMapper jsonMapper;
  protected final JavaType contentType;
  protected final transient JsonApiConfiguration jsonApiConfiguration;

  private final JsonMapper plainJsonMapper;

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

    plainJsonMapper =
        JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).build();
  }

  @Override
  public T deserialize(JsonParser p, DeserializationContext ctxt) {
    boolean isEntityModelCollection = false;
    if (this instanceof JsonApiPagedModelDeserializer
        || this instanceof JsonApiCollectionModelDeserializer) {
      JavaType javaType = contentType.containedType(0);
      if (javaType.getRawClass() == EntityModel.class) {
        isEntityModelCollection = true;
      }
    }
    JsonApiDocument doc = p.readValueAs(JsonApiDocument.class);
    if (doc.getData() instanceof Collection<?>) {
      final boolean isEntityModelCollectionFinal = isEntityModelCollection;
      List<HashMap<String, Object>> collection = (List<HashMap<String, Object>>) doc.getData();
      Assert.notNull(collection, "JsonApiDocument data must not be null!");
      List<Object> resources =
          collection.stream()
              .map(
                  data ->
                      this.convertToResource(data, isEntityModelCollectionFinal, doc, null, false))
              .toList();
      return convertToRepresentationModel(resources, doc);
    }
    HashMap<String, Object> data = (HashMap<String, Object>) doc.getData();
    final Object objectFromProperties = convertToResource(data, false, doc, null, false);

    return convertToRepresentationModel(Collections.singletonList(objectFromProperties), doc);
  }

  @Override
  public ValueDeserializer<?> createContextual(
      DeserializationContext ctxt, @Nullable BeanProperty property) {
    JavaType type =
        property == null ? ctxt.getContextualType() : property.getType().getContentType();
    return createJsonDeserializer(type);
  }

  @Nullable
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

    Object objectFromProperties;
    JavaType rootType = javaType;
    if (rootType == null) {
      rootType = JacksonHelper.findRootType(this.contentType);
    }
    Class<?> clazz = null;

    if (jsonApiConfiguration.isTypeForClassUsedForDeserialization()) {
      String jsonApiType = (String) data.get("type");
      if (jsonApiType != null) {
        clazz = jsonApiConfiguration.getClassForType(jsonApiType);
        if (clazz != null && !rootType.getRawClass().isAssignableFrom(clazz)) {
          throw new IllegalArgumentException(
              clazz + " is not assignable to " + rootType.getRawClass());
        }
        if (clazz != null) {
          rootType = jsonMapper.constructType(clazz);
        }
      }
    }

    if (attributes != null) {
      // we have to use the plain json mapper to not get in conflict with links deserialization
      objectFromProperties = plainJsonMapper.convertValue(attributes, rootType);
    } else {
      try {
        if (useDataForCreation) {
          // we have to use the "real" json mapper due to polymorphic deserialization using Jackson
          objectFromProperties = jsonMapper.convertValue(data, rootType);
        } else {
          if (clazz == null) {
            clazz = rootType.getRawClass();
          }
          objectFromProperties = clazz.getDeclaredConstructor().newInstance();
        }
      } catch (Exception e) {
        throw new IllegalStateException("Cannot convert data to resource.");
      }
    }

    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
        objectFromProperties,
        JsonApiResourceIdentifier.JsonApiResourceField.ID,
        (String) data.get("id"));
    JsonApiResourceIdentifier.setJsonApiResourceFieldAttributeForObject(
        objectFromProperties,
        JsonApiResourceIdentifier.JsonApiResourceField.TYPE,
        (String) data.get("type"));

    if (wrapInEntityModel) {
      Links links = Links.NONE;
      Object linksData = data.get("links");
      if (linksData != null && linksData instanceof Map) {
        // Use JsonApiLinksDeserializer to properly deserialize links
        JsonApiLinksDeserializer linksDeserializer = new JsonApiLinksDeserializer();
        links = linksDeserializer.deserialize((Map<String, Object>) linksData);
      }
      JsonApiEntityModelDeserializer jsonApiEntityModelDeserializer =
          new JsonApiEntityModelDeserializer(jsonApiConfiguration);
      JsonApiDocument jsonApiDocument =
          new JsonApiDocument(
              null, data, null, null, links, doc != null ? doc.getIncluded() : null);
      return jsonApiEntityModelDeserializer.convertToRepresentationModel(
          Collections.singletonList(objectFromProperties), jsonApiDocument);
    }

    return objectFromProperties;
  }

  protected abstract T convertToRepresentationModel(List<Object> resources, JsonApiDocument doc);

  protected abstract ValueDeserializer<?> createJsonDeserializer(JavaType type);
}
