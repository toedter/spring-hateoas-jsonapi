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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

/**
 * Builder API to create complex JSON:API representations exposing a JSON:API idiomatic API.
 * It includes building JSON:API {@literal relationships} and {@literal included}.
 *
 * @author Kai Toedter
 */
@Slf4j
public class JsonApiModelBuilder {

  private static final String RELATIONSHIP_NAME_MUST_NOT_BE_NULL =
    "relationship name must not be null!";
  private static final String RELATED = "related";
  private static final String PAGE = "page";
  private static final String PAGE_NUMBER = "number";
  private static final String PAGE_SIZE = "size";
  private static final String PAGE_TOTAL_ELEMENTS = "totalElements";
  private static final String PAGE_TOTAL_PAGES = "totalPages";

  private final HashMap<String, JsonApiRelationship> relationships =
    new HashMap<>();
  private final HashMap<String, Collection<String>> sparseFieldsets =
    new HashMap<>();
  private final List<RepresentationModel<?>> included = new ArrayList<>();
  private final Map<String, Object> meta = new LinkedHashMap<>();

  private RepresentationModel<?> model;
  private Links links = Links.NONE;

  private JsonApiModelBuilder() {}

  /**
   * Sets the  {@link RepresentationModel} as the base for
   * the {@literal RepresentationModel} to be built.
   * <p>
   * NOTE: If the model is already set, an {@literal IllegalStateException} will be thrown.
   *
   * @param model must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder model(RepresentationModel<?> model) {
    Assert.notNull(model, "RepresentationModel must not be null!");

    if (this.model != null) {
      throw new IllegalStateException("Model object already set!");
    }

    this.model = model;

    this.links(model.getLinks());

    return this;
  }

  /**
   * Creates an {@link EntityModel} from the {@literal object} as the base for
   * the {@literal RepresentationModel} to be built.
   * <p>
   * NOTE: If the model is already set, an {@literal IllegalStateException} will be thrown.
   *
   * @param object must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder model(Object object) {
    return this.model(EntityModel.of(object));
  }

  /**
   * Adds a {@link Link} to the {@link RepresentationModel} to be built.
   * <p>
   * NOTE: This adds it to the top level.
   * If you need a link inside the model you added with {@link #model(RepresentationModel)} method,
   * add it directly to the model.
   *
   * @param link must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder link(Link link) {
    this.links = links.and(link);
    return this;
  }

  /**
   * Adds a {@link Link} with the given href and {@link LinkRelation}
   * to the {@link RepresentationModel} to be built.
   *
   * @param href     must not be {@literal null}.
   * @param relation must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder link(String href, LinkRelation relation) {
    return link(Link.of(href, relation));
  }

  /**
   * Adds the given {@link Link}s to the {@link RepresentationModel} to be built.
   *
   * @param links must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder links(Iterable<Link> links) {
    this.links = this.links.and(links);

    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the {@link Object}.
   * It must be possible to extract the JSON:API id of this object,
   * see <a href="https://toedter.github.io/spring-hateoas-jsonapi/release/reference/#annotations">reference doc</a>.
   * If there is already a relationship for the given name defined,
   * the new data object will be added to the existing relationship.
   * If the dataObject is {@literal null}, an empty to-one relationship
   * is created, see <a href="https://jsonapi.org/format/#document-resource-object-linkage">JSON:API doc</a>.
   *
   * @param name       must not be {@literal null}.
   * @param dataObject must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    @Nullable Object dataObject
  ) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);

    final JsonApiRelationship jsonApiRelationship = addDataObject(
      relationships.get(name),
      dataObject,
      null
    );
    relationships.put(name, jsonApiRelationship);

    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the {@link Collection}.
   * It must be possible to extract the JSON:API id of all elements
   * of this collection,
   * see <a href="https://toedter.github.io/spring-hateoas-jsonapi/release/reference/#annotations">reference doc</a>.
   * If there is already a relationship for the given name defined,
   * the elements of the collection will be added to the existing relationship.
   *
   * @param name       must not be {@literal null}.
   * @param collection must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    Collection<?> collection
  ) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);
    Assert.notNull(
      collection,
      "Relationship data collection must not be null!"
    );

    final JsonApiRelationship jsonApiRelationship = addDataCollection(
      relationships.get(name),
      collection
    );
    relationships.put(name, jsonApiRelationship);

    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the given {@link EntityModel}
   * to the {@link RepresentationModel} to be built.
   * If there is already a relationship for the given name defined,
   * the new {@link EntityModel} will be added to the existing relationship.
   *
   * @param name        must not be {@literal null}.
   * @param entityModel must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    EntityModel<?> entityModel
  ) {
    Assert.notNull(entityModel, "EntityModel must not be null!");
    Object content = entityModel.getContent();
    Assert.notNull(content, "Content of EntityModel must not be null!");
    return this.relationship(name, content);
  }

  /**
   * Adds or updates a {@literal relationship} based on the given {@link EntityModel}
   * and links. A self link of the relation and
   * a related link (to the related resource) can also be added.
   * While entityModel, selfLink, and relatedLink can be null, at least
   * one of them has to be not null.
   *
   * @param name        must not be {@literal null}.
   * @param entityModel can be {@literal null}.
   * @param selfLink    can be {@literal null}.
   * @param relatedLink can be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    @Nullable EntityModel<?> entityModel,
    @Nullable String selfLink,
    @Nullable String relatedLink
  ) {
    Assert.notNull(name, "Relationship name must not be null!");

    if (entityModel == null && selfLink == null && relatedLink == null) {
      throw new IllegalArgumentException(
        "At least one of entityModel, selfLink, and relatedLink must not be null!"
      );
    }

    JsonApiRelationship jsonApiRelationship = null;
    if (entityModel != null) {
      Object content = entityModel.getContent();
      Assert.notNull(content, "Content of EntityModel must not be null!");
      jsonApiRelationship = addDataObject(
        relationships.get(name),
        content,
        null
      );
    }

    if (selfLink != null || relatedLink != null) {
      jsonApiRelationship = replaceLinks(
        jsonApiRelationship,
        selfLink,
        relatedLink,
        null
      );
    }

    relationships.put(name, jsonApiRelationship);
    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the {@literal meta}
   * to the {@link RepresentationModel} to be built.
   * If there is already a relationship for the given name defined,
   * the meta will overwrite the existing relationship.
   *
   * @param name must not be {@literal null}.
   * @param meta must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    Map<String, Object> meta
  ) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);
    Assert.notNull(meta, "relationship meta object must not be null!");

    JsonApiRelationship jsonApiRelationship = relationships.get(name);
    if (jsonApiRelationship == null) {
      jsonApiRelationship = JsonApiRelationship.of(meta);
    } else {
      jsonApiRelationship = jsonApiRelationship.withMeta(meta);
    }
    relationships.put(name, jsonApiRelationship);

    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the {@literal meta}
   * to the {@link RepresentationModel} to be built.
   * If there is already a relationship for the given name defined,
   * the meta will overwrite the existing relationship.
   *
   * @param name                   must not be {@literal null}.
   * @param dataObject             must not be {@literal null}.
   * @param resourceIdentifierMeta can be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    Object dataObject,
    Map<String, Object> resourceIdentifierMeta
  ) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);
    Assert.notNull(dataObject, "Relationship data object must not be null!");

    final JsonApiRelationship jsonApiRelationship = addDataObject(
      relationships.get(name),
      dataObject,
      resourceIdentifierMeta
    );

    relationships.put(name, jsonApiRelationship);
    return this;
  }

  /**
   * Adds or updates a {@literal relationship} based on the links
   * to the {@link RepresentationModel} to be built.
   * If there is already a relationship for the given name defined,
   * the new links will overwrite the existing ones.
   *
   * @param name        must not be {@literal null}.
   * @param selfLink    can be {@literal null}.
   * @param relatedLink can be {@literal null}.
   * @param otherLinks  can be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationship(
    String name,
    @Nullable String selfLink,
    @Nullable String relatedLink,
    @Nullable Links otherLinks
  ) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);

    final JsonApiRelationship jsonApiRelationship = replaceLinks(
      relationships.get(name),
      selfLink,
      relatedLink,
      otherLinks
    );
    relationships.put(name, jsonApiRelationship);

    return this;
  }

  /**
   * If called (anywhere in the builder sequence),
   * the data portion of this relationship will always be rendered
   * as an array, even if the data is not set or is one single element,
   * e.g. {@literal "data": []} or {@literal "data" : [{"id":"1", "type":"movies"}]}.
   * This is convenient if the consumer always expects a (one to many)
   * relationship to be rendered as an array rather than having to check for
   * null values or single objects.
   *
   * @param name must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder relationshipWithDataArray(String name) {
    Assert.notNull(name, RELATIONSHIP_NAME_MUST_NOT_BE_NULL);

    JsonApiRelationship jsonApiRelationship = relationships.get(name);
    if (jsonApiRelationship == null) {
      jsonApiRelationship = new JsonApiRelationship(null, null, null, null);
    }
    jsonApiRelationship = jsonApiRelationship.isAlwaysSerializedWithDataArray();
    relationships.put(name, jsonApiRelationship);

    return this;
  }

  private JsonApiRelationship replaceLinks(
    @Nullable JsonApiRelationship jsonApiRelationship,
    @Nullable String selfLink,
    @Nullable String relatedLink,
    @Nullable Links otherLinks
  ) {
    Links relationshipLinks = Links.NONE;

    if (otherLinks != null) {
      relationshipLinks = otherLinks;
    }

    if (selfLink != null && selfLink.trim().length() != 0) {
      relationshipLinks = relationshipLinks.and(Link.of(selfLink));
    }

    if (relatedLink != null && relatedLink.trim().length() != 0) {
      relationshipLinks = relationshipLinks.and(
        Link.of(relatedLink).withRel(RELATED)
      );
    }

    if (
      relationshipLinks.isEmpty() ||
      !(relationshipLinks.hasLink("self") || relationshipLinks.hasLink(RELATED))
    ) {
      throw new IllegalArgumentException(
        "JSON:API relationship links must contain a \"self\" link or a \"related\" link!"
      );
    }

    JsonApiRelationship newRelationship;
    if (jsonApiRelationship == null) {
      newRelationship = JsonApiRelationship.of(relationshipLinks);
    } else {
      newRelationship = jsonApiRelationship.withLinks(relationshipLinks);
    }
    return newRelationship;
  }

  private JsonApiRelationship addDataObject(
    @Nullable JsonApiRelationship jsonApiRelationship,
    @Nullable Object dataObject,
    @Nullable Map<String, Object> resourceIdentifierMeta
  ) {
    JsonApiRelationship newRelationship;
    if (jsonApiRelationship == null) {
      newRelationship = JsonApiRelationship.of(
        dataObject,
        resourceIdentifierMeta
      );
    } else {
      newRelationship = jsonApiRelationship.addDataObject(
        dataObject,
        resourceIdentifierMeta
      );
    }
    return newRelationship;
  }

  private JsonApiRelationship addDataCollection(
    @Nullable JsonApiRelationship jsonApiRelationship,
    Collection<?> collection
  ) {
    JsonApiRelationship newRelationship;
    if (jsonApiRelationship == null) {
      newRelationship = JsonApiRelationship.of(collection);
    } else {
      newRelationship = jsonApiRelationship.addDataCollection(collection);
    }
    return newRelationship;
  }

  /**
   * Adds the given {@link RepresentationModel}
   * to the {@literal included} {@link RepresentationModel}s.
   * It will appear then top level in the {@literal JSON:API included} entities.
   * Duplicates with same {@literal id} and {@literal type} will be eliminated.
   *
   * @param representationModel must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder included(
    RepresentationModel<?> representationModel
  ) {
    included.add(representationModel);
    return this;
  }

  /**
   * Adds the given {@link Object}
   * to the {@literal included} {@link RepresentationModel}s.
   * The object is automatically wrapped into an {@link EntityModel}.
   * It will appear then top level in the {@literal JSON:API included} entities.
   * Duplicates with same {@literal id} and {@literal type} will be eliminated.
   *
   * @param object must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder included(Object object) {
    return this.included(EntityModel.of(object));
  }

  /**
   * Adds the given {@link Collection}
   * to the {@literal included} {@link RepresentationModel}s.
   * The objects of the collection are automatically wrapped into an {@link EntityModel},
   * if they are not already {@link RepresentationModel}s.
   * The members of the collection will appear then top level in the {@literal JSON:API included} entities.
   * Duplicates with same {@literal id} and {@literal type} will be eliminated.
   *
   * @param collection must not be {@literal null}.
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder included(Collection<?> collection) {
    Assert.notNull(collection, "included data collection must not be null!");
    for (Object object : collection) {
      if (object instanceof RepresentationModel<?>) {
        this.included((RepresentationModel<?>) object);
      } else {
        this.included(object);
      }
    }
    return this;
  }

  /**
   * Adds the given key/value pair to the {@literal JSON:API} meta.
   *
   * @param key   the json key
   * @param value the json value
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder meta(String key, Object value) {
    this.meta.put(key, value);
    return this;
  }

  /**
   * Adds the paging information to the {@literal JSON:API} meta.
   * Preconditions are:
   * - the model has been added before
   * - the model is a paged model
   * - the model contains a Pageable
   *
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder pageMeta() {
    final PagedModel.PageMetadata metadata = getPageMetadata();

    final long pageNumber = metadata.getNumber();
    final long pageSize = metadata.getSize();
    final long totalElements = metadata.getTotalElements();
    final long totalPages = metadata.getTotalPages();

    Map<String, Object> metaObject = new LinkedHashMap<>();
    metaObject.put(PAGE_SIZE, pageSize);
    metaObject.put(PAGE_TOTAL_ELEMENTS, totalElements);
    metaObject.put(PAGE_TOTAL_PAGES, totalPages);
    metaObject.put(PAGE_NUMBER, pageNumber);

    meta.put(PAGE, metaObject);

    return this;
  }

  /**
   * Creates all pagination links with {@literal JSON:API} default request parameters for
   * page number {@literal page[number]} and page size {@literal page[size]}.
   * <p>
   * Preconditions are:<ul>
   * <li>the model has been added before
   * <li>the model is a {@literal PagedModel}
   * <li>the model contains {@literal PageMetadata}
   * </ul>
   *
   * @param linkBase the prefix of all pagination links, e.g. the base URL of the collection resource
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder pageLinks(String linkBase) {
    return this.pageLinks(linkBase, "page[number]", "page[size]");
  }

  /**
   * Creates all pagination links.
   * <p>
   * Preconditions are:<ul>
   * <li>the model has been added before
   * <li>the model is a {@literal PagedModel}
   * <li>the model contains {@literal PageMetadata}
   * </ul>
   *
   * @param linkBase               the prefix of all pagination links, e.g. the base URL of the collection resource
   * @param pageNumberRequestParam the request parameter for page number
   * @param pageSizeRequestParam   the request parameter for page size
   * @return will never be {@literal null}.
   */
  public JsonApiModelBuilder pageLinks(
    String linkBase,
    String pageNumberRequestParam,
    String pageSizeRequestParam
  ) {
    Assert.notNull(linkBase, "link base for paging must not be null!");
    Assert.notNull(
      pageNumberRequestParam,
      "page number request parameter must not be null!"
    );
    Assert.notNull(
      pageSizeRequestParam,
      "page size request parameter must not be null!"
    );

    final PagedModel.PageMetadata metadata = getPageMetadata();

    final long pageNumber = metadata.getNumber();
    final long pageSize = metadata.getSize();
    final long totalPages = metadata.getTotalPages();

    List<Link> paginationLinks = new ArrayList<>();

    String paramStart = "?";

    try {
      URL url = new URL(linkBase);
      String query = url.getQuery();
      if (query != null) {
        paramStart = "&";
      }
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException(
        "linkBase parameter must be a valid URL."
      );
    }

    if (pageNumber > 0) {
      Link firstLink = Link.of(
        linkBase +
          paramStart +
          pageNumberRequestParam +
          "=0&" +
          pageSizeRequestParam +
          "=" +
          pageSize
      ).withRel(IanaLinkRelations.FIRST);
      paginationLinks.add(firstLink);

      Link prevLink = Link.of(
        linkBase +
          paramStart +
          pageNumberRequestParam +
          "=" +
          (pageNumber - 1) +
          "&" +
          pageSizeRequestParam +
          "=" +
          pageSize
      ).withRel(IanaLinkRelations.PREV);
      paginationLinks.add(prevLink);
    }

    if (pageNumber < totalPages - 1) {
      Link nextLink = Link.of(
        linkBase +
          paramStart +
          pageNumberRequestParam +
          "=" +
          (pageNumber + 1) +
          "&" +
          pageSizeRequestParam +
          "=" +
          pageSize
      ).withRel(IanaLinkRelations.NEXT);
      paginationLinks.add(nextLink);

      Link lastLink = Link.of(
        linkBase +
          paramStart +
          pageNumberRequestParam +
          "=" +
          (totalPages - 1) +
          "&" +
          pageSizeRequestParam +
          "=" +
          pageSize
      ).withRel(IanaLinkRelations.LAST);
      paginationLinks.add(lastLink);
    }

    this.links = this.links.and(paginationLinks);

    return this;
  }

  /**
   * Adds a sparse fieldset for the given JSON:API type.
   * Only the resource objects attributes that are in the fields
   * parameters will be serialized to JSON. THis will apply to data
   * attributes and attributes of included resources.
   * This will not exclude relationships, if the name of a relationship
   * for the given JSON:API type is not part of the fields parameters.
   *
   * @param jsonapiType the JSON:API type
   * @param fields      the attributes that should be included
   * @return ill never be {@literal null}.
   */
  public JsonApiModelBuilder fields(String jsonapiType, String... fields) {
    List<String> fieldList = new ArrayList<>(Arrays.asList(fields));
    sparseFieldsets.put(jsonapiType, fieldList);
    return this;
  }

  /**
   * Transform the entities, Links, relationships and included
   * into a {@link RepresentationModel}.
   *
   * @return will never be {@literal null}.
   */
  public RepresentationModel<?> build() {
    for (JsonApiRelationship jsonApiRelationship : relationships.values()) {
      if (!jsonApiRelationship.isValid()) {
        throw new IllegalStateException(
          "Cannot build representation model: JSON:API relationship validation error for: " +
            jsonApiRelationship
        );
      }
    }
    return new JsonApiModel(
      model,
      relationships,
      included,
      meta,
      links,
      sparseFieldsets
    );
  }

  /**
   * Creates a new {@link JsonApiModelBuilder}.
   *
   * @return will never be {@literal null}.
   */
  public static JsonApiModelBuilder jsonApiModel() {
    return new JsonApiModelBuilder();
  }

  private PagedModel.PageMetadata getPageMetadata() {
    if (this.model == null) {
      throw new IllegalStateException("Model object (PagedModel) must be set.");
    }

    if (!(this.model instanceof PagedModel)) {
      throw new IllegalStateException("Model object must be a PagedModel.");
    }

    final PagedModel.PageMetadata metadata = ((PagedModel<
        ?
      >) model).getMetadata();

    if (metadata == null) {
      throw new IllegalStateException(
        "PagedModel object must contain page metadata."
      );
    }

    return metadata;
  }
}
