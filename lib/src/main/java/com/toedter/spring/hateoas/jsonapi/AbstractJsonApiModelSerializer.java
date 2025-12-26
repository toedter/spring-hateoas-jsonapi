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

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.java.Log;
import org.jspecify.annotations.Nullable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

@Log
abstract class AbstractJsonApiModelSerializer<T extends RepresentationModel<?>>
    extends AbstractJsonApiSerializer<T> {

  private final JsonApiConfiguration jsonApiConfiguration;
  private final JsonMapper jsonMapper;

  private static class JsonApiDocumentWithoutSerializedData extends JsonApiDocument {

    JsonApiDocumentWithoutSerializedData(JsonApiDocument jsonApiDocument) {
      super(
          jsonApiDocument.getJsonapi(),
          null,
          jsonApiDocument.getMeta(),
          jsonApiDocument.getErrors(),
          jsonApiDocument.getLinks(),
          jsonApiDocument.getIncluded());
    }

    @Override
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    public @Nullable Object getData() {
      return null;
    }
  }

  protected AbstractJsonApiModelSerializer(Class<?> t, JsonApiConfiguration jsonApiConfiguration) {
    super(jsonApiConfiguration.getJsonMapper().constructType(t));
    this.jsonApiConfiguration = jsonApiConfiguration;
    this.jsonMapper = jsonApiConfiguration.getJsonMapper();
  }

  @Override
  public void serialize(T value, JsonGenerator gen, SerializationContext provider) {
    CollectionModel<?> collectionModel = extractCollectionModel(value);
    SerializationData serializationData = extractSerializationData(value, collectionModel);

    Links documentLevelLinks = determineDocumentLevelLinks(value);

    JsonApiDocument doc = buildJsonApiDocument(serializationData, documentLevelLinks, value);
    doc = addJsonApiObjectIfNeeded(doc);
    doc = addPageMetaIfNeeded(doc, collectionModel);
    doc = addModelMetaIfNeeded(doc, value, serializationData);

    serializeDocument(doc, gen, provider);
  }

  private @Nullable CollectionModel<?> extractCollectionModel(T value) {
    if (value instanceof JsonApiModel jsonApiModel) {
      Object content = jsonApiModel.getContent();
      if (content instanceof CollectionModel<?> collectionModelContent) {
        return collectionModelContent;
      }
    } else if (value instanceof CollectionModel<?> collectionModelContent) {
      return collectionModelContent;
    }
    return null;
  }

  private SerializationData extractSerializationData(
      T value, @Nullable CollectionModel<?> collectionModel) {
    if (collectionModel != null) {
      Object data =
          JsonApiData.extractCollectionContent(
              collectionModel, jsonMapper, jsonApiConfiguration, null, false);
      return new SerializationData(data, null);
    }

    if (value instanceof JsonApiModel jsonApiModel
        && jsonApiModel.getContent() instanceof JsonApiModel content) {
      Map<String, Object> embeddedMeta = content.getMetaData();
      Optional<JsonApiData> jsonApiData =
          JsonApiData.extractContent(content, true, jsonMapper, jsonApiConfiguration, null);
      return new SerializationData(jsonApiData.orElse(null), embeddedMeta);
    }

    Map<String, Object> embeddedMeta = null;
    if (value instanceof JsonApiModel jsonApiModel) {
      embeddedMeta = jsonApiModel.getMetaData();
    }
    Optional<JsonApiData> jsonApiData =
        JsonApiData.extractContent(value, true, jsonMapper, jsonApiConfiguration, null);
    return new SerializationData(jsonApiData.orElse(null), embeddedMeta);
  }

  private @Nullable Links determineDocumentLevelLinks(T value) {
    Links documentLevelLinks = getLinksOrNull(value);
    if (jsonApiConfiguration.isLinksAtResourceLevel() && !(value instanceof CollectionModel)) {
      // For single resources, links will be at resource level, so don't include at document level
      return null;
    }
    return documentLevelLinks;
  }

  private JsonApiDocument buildJsonApiDocument(
      SerializationData serializationData, @Nullable Links documentLevelLinks, T value) {
    return new JsonApiDocument()
        .withData(serializationData.data())
        .withLinks(documentLevelLinks)
        .withIncluded(getIncluded(value));
  }

  private JsonApiDocument addJsonApiObjectIfNeeded(JsonApiDocument doc) {
    JsonApiObject jsonApiObject = jsonApiConfiguration.getJsonApiObject();
    if (jsonApiObject != null
        && (jsonApiObject.getVersion() != null
            || jsonApiObject.getExt() != null
            || jsonApiObject.getProfile() != null
            || jsonApiObject.getMeta() != null)) {
      return doc.withJsonapi(jsonApiObject);
    }
    return doc;
  }

  private JsonApiDocument addPageMetaIfNeeded(
      JsonApiDocument doc, @Nullable CollectionModel<?> collectionModel) {
    if (jsonApiConfiguration.isPageMetaAutomaticallyCreated()
        && collectionModel instanceof PagedModel) {
      JsonApiModel model =
          (JsonApiModel)
              JsonApiModelBuilder.jsonApiModel().model(collectionModel).pageMeta().build();
      Map<String, Object> metaData = model.getMetaData();
      return doc.withMeta(metaData);
    }
    return doc;
  }

  private JsonApiDocument addModelMetaIfNeeded(
      JsonApiDocument doc, T value, SerializationData serializationData) {
    if (!(value instanceof JsonApiModel jsonApiModel)) {
      return doc;
    }

    Map<String, Object> metaData = jsonApiModel.getMetaData();
    if (metaData == null) {
      return doc;
    }

    if (serializationData.embeddedMeta() != metaData || serializationData.data() == null) {
      Map<String, Object> meta = doc.getMeta();
      if (meta == null) {
        return doc.withMeta(metaData);
      } else {
        // add/override with metadata created with builder
        // this will override the previous generated page metadata, if the key is the same
        for (Map.Entry<?, ?> entry : metaData.entrySet()) {
          meta.put(entry.getKey().toString(), entry.getValue());
        }
      }
    }
    return doc;
  }

  private void serializeDocument(
      JsonApiDocument doc, JsonGenerator gen, SerializationContext provider) {
    // issue #13: if meta is set, we don't want to serialize to "data": null
    Map<String, Object> meta = doc.getMeta();
    if (meta != null && !meta.isEmpty() && doc.getData() == null) {
      JsonApiDocumentWithoutSerializedData documentWithoutSerializedData =
          new JsonApiDocumentWithoutSerializedData(doc);
      provider
          .findValueSerializer(JsonApiDocumentWithoutSerializedData.class)
          .serialize(documentWithoutSerializedData, gen, provider);
    } else {
      provider.findValueSerializer(JsonApiDocument.class).serialize(doc, gen, provider);
    }
  }

  private record SerializationData(
      @Nullable Object data, @Nullable Map<String, Object> embeddedMeta) {}

  private @Nullable Links getLinksOrNull(RepresentationModel<?> representationModel) {
    Links links = representationModel.getLinks();
    if (links.isEmpty()) {
      return null;
    }

    // breaking change: JSON:API only allows specific links at (document) top-level!,
    // see https://jsonapi.org/format/#document-top-level.
    // Those links are self, related, describedby, and
    // the pagination links first, last, prev, and next.
    // All other top-level links are not allowed and therefore removed.
    if (jsonApiConfiguration.isJsonApiCompliantLinks()) {
      return filterValidJsonApiLinks(links);
    }

    return links;
  }

  private Links filterValidJsonApiLinks(Links links) {
    Links validJsonApiLinks = Links.NONE;
    for (Link link : links) {
      if (isValidJsonApiTopLevelLink(link, validJsonApiLinks)) {
        validJsonApiLinks = validJsonApiLinks.and(link);
      } else {
        log.warning("removed invalid JSON:API top-level link: " + link.getRel());
      }
    }
    return validJsonApiLinks;
  }

  private boolean isValidJsonApiTopLevelLink(Link link, Links validJsonApiLinks) {
    return !validJsonApiLinks.hasLink(link.getRel())
        && (link.hasRel("self")
            || link.hasRel("related")
            || link.hasRel("describedby")
            || link.hasRel("first")
            || link.hasRel("last")
            || link.hasRel("prev")
            || link.hasRel("next"));
  }

  private @Nullable List<JsonApiData> getIncluded(RepresentationModel<?> representationModel) {
    if (representationModel instanceof JsonApiModel jsonApiModel) {
      final List<RepresentationModel<?>> includedEntities = jsonApiModel.getIncludedEntities();
      if (includedEntities == null) {
        return null;
      }
      final CollectionModel<RepresentationModel<?>> collectionModel =
          CollectionModel.of(includedEntities);
      return JsonApiData.extractCollectionContent(
          collectionModel,
          jsonMapper,
          jsonApiConfiguration,
          jsonApiModel.getSparseFieldsets(),
          true);
    }
    return null;
  }
}
