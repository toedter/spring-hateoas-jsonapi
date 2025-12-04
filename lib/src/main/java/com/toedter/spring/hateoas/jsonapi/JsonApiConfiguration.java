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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.hateoas.LinkRelation;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * JSON:API specific configuration.
 *
 * @author Kai Toedter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonApiConfiguration {

  /**
   * The list of possible affordance types.
   * Those affordance types are used by {@link withAffordancesRenderedAsLinkMeta}.
   */
  @SuppressWarnings("JavadocReference")
  public enum AffordanceType {
    /**
     * Default, affordances will NOT be rendered as link meta.
     */
    NONE,
    /**
     * Affordances will be rendered as link meta (proprietary format).
     * The format is close to the internal Spring HATEOAS internal model.
     */
    SPRING_HATEOAS,
    /**
     * Affordances will be rendered as link meta (HAL-FORMS format).
     * See <a href="https://rwcbook.github.io/hal-forms/#templates-element">https://rwcbook.github.io/hal-forms/#templates-element</a>.
     */
    HAL_FORMS,
  }

  /**
   * Indicates if the JSON:API type attribute of resource objects is pluralized.
   *
   * @param pluralizedTypeRendered The new value of this configuration's pluralizedTypeRendered
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean pluralizedTypeRendered;

  /**
   * Indicates if the JSON:API type attribute of resource objects is lower-cased.
   *
   * @param lowerCasedTypeRendered The new value of this configuration's lowerCasedTypeRendered
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean lowerCasedTypeRendered;

  /**
   * Indicates if the JSON:API version is rendered.
   * <p>
   * If set to true, each rendered JSON:API document will start with
   * </p>
   * <pre>
   * "jsonapi": {
   *    "version": "1.1"
   * }
   * </pre>
   *
   * @param jsonApiVersionRendered The new value of this configuration's jsonApiVersionRendered
   * @return The default is {@literal false}.
   * @deprecated since 2.0.0, prefer {@link #jsonApiObject}
   */
  @With
  @Getter
  @Deprecated(since = "2.0.0")
  private final boolean jsonApiVersionRendered;

  /**
   * Indicates if the JSON:API object is rendered.
   * <p>
   * If set, each rendered JSON:API document will start with a JSON:API object like
   * </p>
   * <pre>
   * {
   *   "jsonapi": {
   *     "version": "1.1",
   *     "ext": [
   *       "https://jsonapi.org/ext/atomic"
   *     ],
   *     "profile": [
   *       "http://example.com/profiles/flexible-pagination",
   *       "http://example.com/profiles/resource-versioning"
   *     ]
   *   }
   * }
   * </pre>
   * See also <a href="https://jsonapi.org/format/#document-jsonapi-object">JSON:API Object</a>.
   *
   * @param jsonApiObject The new value of this configuration's jsonApiObject
   * @return The default is {@literal false}.
   */
  @With
  @Getter
  private final JsonApiObject jsonApiObject;

  /**
   * Indicates if page metadata (rendered as top level JSON:API meta)
   * for a paged model is created automatically.
   *
   * @param pageMetaAutomaticallyCreated The new value of this configuration's pageMetaAutomaticallyCreated
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean pageMetaAutomaticallyCreated;

  /**
   * Indicates if the Java class to JSON:API mapping created with {@link JsonApiConfiguration}
   * is also used during deserialization.
   *
   * @param typeForClassUsedForDeserialization The new value of this configuration's typeForClassUsedForDeserialization
   * @return The default is {@literal false}.
   */
  @With
  @Getter
  private final boolean typeForClassUsedForDeserialization;

  /**
   * Indicates if Spring HATEOAS affordances are rendered as JSON:API link meta.
   * This feature is experimental, please don't use it in production yet.
   * The format of the affordances will probably change. Currently, only a proprietary
   * format ({@literal SPRING_HATEOAS}) derived from the internal affordance model
   * and {@literal HAL-FORMS} _templates are supported.
   *
   * @param affordancesRenderedAsLinkMeta The new value of this configuration's affordancesRenderedAsLinkMeta
   * @return The list of {@link AffordanceType}. The default is NONE, so no affordances will be rendered.
   */
  @With
  @Getter
  private final AffordanceType affordancesRenderedAsLinkMeta;

  /**
   * Indicates if empty attributes are serialized as empty object.
   * <p>
   * If set to true, empty attributes are serialized as
   * </p>
   * <pre>
   * {
   *   "data": {
   *      "id": "1",
   *      "type": "movies",
   *      "attributes": {
   *      }
   *   }
   * }
   * </pre>
   * <p>
   * If set to false, attributes are not serialized,
   * </p>
   * <pre>
   * {
   *   "data": {
   *      "id": "1",
   *      "type": "movies"
   *   }
   * }
   * </pre>
   *
   * @param emptyAttributesObjectSerialized The new value of this configuration's emptyAttributesObjectSerialized
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean emptyAttributesObjectSerialized;

  /**
   * If you want to create JSON for a POST request that does not contain the {@literal id} attribute.
   * For example, if you set this property to "doNotSerialize" and initialize a Movie object with id = "doNotSerialize",
   * the serialized JSON would look like
   * <pre>
   * {
   *   "data": {
   *      "type": "movies",
   *      "attributes": {
   *         "title": "Star Wars"
   *       }
   *    }
   * }
   * </pre>
   *
   * @param jsonApiIdNotSerializedForValue The value of the JSON:API resource id that is ignored for serialization
   * @return The default is {@literal null}.
   */
  @With
  @Getter
  private final String jsonApiIdNotSerializedForValue;

  /**
   * You can pass a lambda expression to customize the ObjectMapper used
   * for serialization.
   *
   * @param objectMapperCustomizer the ObjectMapper customizer
   */
  @With
  private final Consumer<ObjectMapper> objectMapperCustomizer;

  /**
   * JSON:API 1.1 introduced the possible link properties title, type, and hreflang (and some more),
   * see <a href="https://jsonapi.org/format/#auto-id--link-objects">JSON:API link objects</a>.
   * Since title, type and hreflang exist also in the Spring HATEOAS link model, for JSON:API 1.1 they
   * are now serialized as direct link properties. For the previous version JSON:API v1.0
   * they were serialized in the meta section.
   * <p>
   * The removal from the meta section is a breaking change that could harm existing clients.
   * But to keep the format backward-compatible to previous versions of this library, title, type, and hreflang
   * can still be rendered also in the link's meta section. For backward-compatible behavior, set this configuration
   * to {@literal false}.
   *
   * @param jsonApi11LinkPropertiesRemovedFromLinkMeta The new value of this configuration's jsonApi11LinkPropertiesRemovedFromLinkMeta
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean jsonApi11LinkPropertiesRemovedFromLinkMeta;

  /**
   * JSON:API is very strict about the allowed link relations, the allowed
   * <a href="https://jsonapi.org/format/#document-top-level">top-level links</a> are
   * self, related, describedBy, next, pre, first and last. The only allowed
   * <a href="https://jsonapi.org/format/#document-resource-object-links">resource link</a> is self.
   * <p>
   * If you set this configuration to {@literal false}, Spring HATEOAS links that are not compliant
   * with JSON:API would also be serialized.
   *
   * @param jsonApiCompliantLinks The new value of this configuration's jsonApiCompliantLinks
   * @return The default is {@literal true}.
   */
  @With
  @Getter
  private final boolean jsonApiCompliantLinks;

  /**
   * By default, JSON:API links are serialized as URL encoded.
   * <p>
   * If you set this configuration, Spring HATEOAS links with set relations won't be URL encoded.
   * This can be useful for instance to avoid double uri encoding when you pass the links to the model already
   * URL encoded.
   *
   * @param linksNotUrlEncoded The new value of this configuration's linksNotUrlEncoded
   * @return The default is an empty set.
   */
  @With
  @Getter
  private final Set<LinkRelation> linksNotUrlEncoded;

  /**
   * Controls where links are placed in JSON:API documents for single resource (EntityModel) serialization.
   * <p>
   * According to the JSON:API specification, links can be placed at:
   * <ul>
   *   <li>Document level (top-level) - the default behavior</li>
   *   <li>Resource level (within the resource object in the "data" section)</li>
   * </ul>
   * <p>
   * When set to {@literal true}, links from EntityModel are placed at the resource level
   * (inside the resource object). When set to {@literal false}, links are placed at the
   * document level (top-level).
   * <p>
   * Note: This configuration only affects single resource serialization (EntityModel).
   * Collection resources and relationships are not affected by this setting.
   *
   * @param linksAsResourceLevelLinks The new value of this configuration's linksAsResourceLevelLinks
   * @return The default is {@literal false} (links at document level).
   */
  @With
  @Getter
  private final boolean linksAsResourceLevelLinks;

  @With(AccessLevel.PRIVATE)
  private final Map<Class<?>, String> typeForClass;

  private ObjectMapper objectMapper;

  /**
   * Customizes the object mapper if a customizer was set with
   * {@literal withObjectMapperCustomizer}.
   *
   * @param objectMapper the object mapper to be customized
   * @return this JsonApiConfiguration
   */
  public JsonApiConfiguration customize(ObjectMapper objectMapper) {
    this.objectMapperCustomizer.accept(objectMapper);
    return this;
  }

  /*
     This method is used only internally by the deserializers
     */
  ObjectMapper getObjectMapper() {
    return objectMapper;
  }

  /*
      This method is used only internally by the deserializers
     */
  void setObjectMapper(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Creates a mapping for a given class to get the JSON:API resource object {@literal type}
   * when rendered.
   *
   * @param clazz must not be {@literal null}.
   * @param type  must not be {@literal null}.
   * @return a clone of this object, except with this updated property
   */
  public JsonApiConfiguration withTypeForClass(Class<?> clazz, String type) {
    Assert.notNull(clazz, "class must not be null!");
    Assert.notNull(type, "type must not be null!");

    Map<Class<?>, String> map = new LinkedHashMap<>(typeForClass);
    map.put(clazz, type);

    return withTypeForClass(map);
  }

  /**
   * Returns the {@literal JSON:API resource object type}
   * for a given class, when it was added with {@link #withTypeForClass(Class, String)}.
   *
   * @param clazz must not be {@literal null}.
   * @return can return {@literal null}.
   */
  public @Nullable String getTypeForClass(Class<?> clazz) {
    Assert.notNull(clazz, "class must not be null!");
    return typeForClass.get(clazz);
  }

  /**
   * Returns the {@literal class}
   * for a given type, when the class was added with {@link #withTypeForClass(Class, String)}.
   *
   * @param type must not be {@literal null}.
   * @return can return {@literal null}.
   */
  public @Nullable Class<?> getClassForType(String type) {
    Assert.notNull(type, "type must not be null!");
    if (this.typeForClass.containsValue(type)) {
      for (Map.Entry<Class<?>, String> entry : this.typeForClass.entrySet()) {
        if (entry.getValue().equals(type)) {
          return entry.getKey();
        }
      }
    }
    return null;
  }

  /**
   * Creates a new default {@link JsonApiConfiguration}.
   */
  public JsonApiConfiguration() {
    this.pluralizedTypeRendered = true;
    this.lowerCasedTypeRendered = true;
    this.jsonApiVersionRendered = false;
    this.jsonApiObject = null;
    this.pageMetaAutomaticallyCreated = true;
    this.typeForClass = new LinkedHashMap<>();
    this.typeForClassUsedForDeserialization = false;
    this.emptyAttributesObjectSerialized = false;
    this.jsonApiIdNotSerializedForValue = null;
    this.affordancesRenderedAsLinkMeta = AffordanceType.NONE;
    this.jsonApi11LinkPropertiesRemovedFromLinkMeta = true;
    this.jsonApiCompliantLinks = true;
    this.linksNotUrlEncoded = new HashSet<>();
    this.linksAsResourceLevelLinks = false;
    this.objectMapperCustomizer = customObjectMapper -> {}; // Default to no action.
  }
}
