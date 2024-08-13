/*
 * Copyright 2023 the original author or authors.
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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.java.Log;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Log
abstract class AbstractJsonApiModelSerializer<T extends RepresentationModel<?>>
        extends AbstractJsonApiSerializer<T> {

    private final transient JsonApiConfiguration jsonApiConfiguration;
    private final ObjectMapper objectMapper;

    private static class JsonApiDocumentWithoutSerializedData extends JsonApiDocument {
        JsonApiDocumentWithoutSerializedData(JsonApiDocument jsonApiDocument) {
            super(jsonApiDocument.getJsonapi(), null,
                    jsonApiDocument.getMeta(), jsonApiDocument.getErrors(),
                    jsonApiDocument.getLinks(), jsonApiDocument.getIncluded());
        }

        @Override
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public @Nullable
        Object getData() {
            return null;
        }
    }

    protected AbstractJsonApiModelSerializer(Class<?> t, JsonApiConfiguration jsonApiConfiguration) {
        super(t, false);
        this.jsonApiConfiguration = jsonApiConfiguration;
        this.objectMapper = new ObjectMapper();
        jsonApiConfiguration.customize(objectMapper);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        CollectionModel<?> collectionModel = null;
        if (value instanceof JsonApiModel jsonApiModel) {
            Object content = jsonApiModel.getContent();
            if (content instanceof CollectionModel<?> collectionModelContent) {
                collectionModel = collectionModelContent;
            }
        } else if (value instanceof CollectionModel<?> collectionModelContent) {
            collectionModel = collectionModelContent;
        }

        Object data;
        Map<String, Object> embeddedMeta = null;

        if (collectionModel != null) {
            data = JsonApiData.extractCollectionContent(
                    collectionModel, objectMapper, jsonApiConfiguration, null, false);
        } else {
            if (value instanceof JsonApiModel jsonApiModel
                    && jsonApiModel.getContent() != null
                    && jsonApiModel.getContent() instanceof JsonApiModel) {
                JsonApiModel content = (JsonApiModel) ((JsonApiModel) value).getContent();
                embeddedMeta = Objects.requireNonNull(content).getMetaData();
                final Optional<JsonApiData> jsonApiData =
                        JsonApiData.extractContent(content, true, objectMapper, jsonApiConfiguration, null);
                data = jsonApiData.orElse(null);
            } else {
                if (value instanceof JsonApiModel jsonApiModel) {
                    embeddedMeta = jsonApiModel.getMetaData();
                }
                final Optional<JsonApiData> jsonApiData =
                        JsonApiData.extractContent(value, true, objectMapper, jsonApiConfiguration, null);
                data = jsonApiData.orElse(null);
            }
        }

        JsonApiDocument doc = new JsonApiDocument()
                .withData(data)
                .withLinks(getLinksOrNull(value))
                .withIncluded(getIncluded(value));

        if (jsonApiConfiguration.isJsonApiVersionRendered()) {
            doc = doc.withJsonapi(new JsonApiObject(true));
        }

        JsonApiObject jsonApiObject = jsonApiConfiguration.getJsonApiObject();
        if (jsonApiObject != null && (jsonApiObject.getVersion() != null
                || jsonApiObject.getExt() != null
                || jsonApiObject.getProfile() != null
                || jsonApiObject.getMeta() != null)) {
            doc = doc.withJsonapi(jsonApiObject);
        }

        if (jsonApiConfiguration.isPageMetaAutomaticallyCreated() && collectionModel instanceof PagedModel) {
            JsonApiModel model =
                    (JsonApiModel) JsonApiModelBuilder.jsonApiModel().model(collectionModel).pageMeta().build();
            Map<String, Object> metaData = model.getMetaData();
            doc = doc.withMeta(metaData);
        }

        if (value instanceof JsonApiModel jsonApiModel) {
            // in some cases we want to add the metadata to the top level JSON:API document
            Map<String, Object> metaData = jsonApiModel.getMetaData();
            if (embeddedMeta != metaData || data == null) {
                final Map<String, Object> meta = doc.getMeta();
                if (meta == null) {
                    doc = doc.withMeta(metaData);
                } else {
                    // add/override with metadata created with builder
                    // this will override the previous generated page metadata, if the key is the same
                    for (Map.Entry<?, ?> entry : metaData.entrySet()) {
                        meta.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
        }

        // issue #13: if meta is set, we don't want to serialize to "data": null
        final Map<String, Object> meta = doc.getMeta();
        if (meta != null && !meta.isEmpty() && doc.getData() == null) {
            JsonApiDocumentWithoutSerializedData documentWithoutSerializedData =
                    new JsonApiDocumentWithoutSerializedData(doc);
            provider
                    .findValueSerializer(JsonApiDocumentWithoutSerializedData.class)
                    .serialize(documentWithoutSerializedData, gen, provider);
        } else {
            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    private @Nullable
    Links getLinksOrNull(RepresentationModel<?> representationModel) {
        Links links = representationModel.getLinks();
        if (links.isEmpty()) {
            links = null;
        }

        // breaking change: JSON:API only allows specific links at (document) top-level!,
        // see https://jsonapi.org/format/#document-top-level.
        // Those links are self, related, describedby, and
        // the pagination links first, last, prev, and next.
        // All other top-level links are not allowed and therefore removed.
        if (jsonApiConfiguration.isJsonApiCompliantLinks() && links != null) {
            Links validJsonApiLinks = Links.NONE;
            for (Link link : links) {
                if (!validJsonApiLinks.hasLink(link.getRel())
                        && (link.hasRel("self") || link.hasRel("related") || link.hasRel("describedby")
                        || link.hasRel("first") || link.hasRel("last") || link.hasRel("prev")
                        || link.hasRel("next"))) {
                    validJsonApiLinks = validJsonApiLinks.and(link);
                } else {
                    log.warning("removed invalid JSON:API top-level link: " + link.getRel());
                }
            }
            links = validJsonApiLinks;
        }

        return links;
    }

    private @Nullable
    List<JsonApiData> getIncluded(RepresentationModel<?> representationModel) {
        if (representationModel instanceof JsonApiModel jsonApiModel) {
            final List<RepresentationModel<?>> includedEntities =
                    jsonApiModel.getIncludedEntities();
            final CollectionModel<RepresentationModel<?>> collectionModel = CollectionModel.of(includedEntities);
            return JsonApiData.extractCollectionContent(
                    collectionModel, objectMapper, jsonApiConfiguration,
                    jsonApiModel.getSparseFieldsets(), true);
        }
        return null;
    }
}
