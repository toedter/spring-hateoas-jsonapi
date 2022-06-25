/*
 * Copyright 2022 the original author or authors.
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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

abstract class AbstractJsonApiModelSerializer<T extends RepresentationModel<?>>
        extends AbstractJsonApiSerializer<T> {

    private final JsonApiConfiguration jsonApiConfiguration;
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
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        RepresentationModel<?> inputValue = value;

        if(inputValue instanceof JsonApiEntityModel) {
            inputValue = ((JsonApiEntityModel)inputValue).getJsonApiModel();
        }

        CollectionModel<?> collectionModel = null;
        if (inputValue instanceof JsonApiModel) {
            Object content = ((JsonApiModel) inputValue).getContent();
            if (content instanceof CollectionModel) {
                collectionModel = (CollectionModel<?>) content;
            }
        } else if (inputValue instanceof CollectionModel<?>) {
            collectionModel = (CollectionModel<?>) inputValue;
        }

        Object data;
        Map<String, Object> embeddedMeta = null;

        if (collectionModel != null) {
            data = JsonApiData.extractCollectionContent(
                    collectionModel, objectMapper, jsonApiConfiguration, null, false);
        } else {
            if (inputValue instanceof JsonApiModel
                    && ((JsonApiModel) inputValue).getContent() != null
                    && ((JsonApiModel) inputValue).getContent() instanceof JsonApiModel) {
                JsonApiModel content = (JsonApiModel) ((JsonApiModel) inputValue).getContent();
                embeddedMeta = Objects.requireNonNull(content).getMetaData();
                final Optional<JsonApiData> jsonApiData =
                        JsonApiData.extractContent(content, true, objectMapper, jsonApiConfiguration, null);
                data = jsonApiData.orElse(null);
            } else {
                if (inputValue instanceof JsonApiModel) {
                    embeddedMeta = ((JsonApiModel) inputValue).getMetaData();
                }
                final Optional<JsonApiData> jsonApiData =
                        JsonApiData.extractContent(inputValue, true, objectMapper, jsonApiConfiguration, null);
                data = jsonApiData.orElse(null);
            }
        }

        JsonApiDocument doc = new JsonApiDocument()
                .withData(data)
                .withLinks(getLinksOrNull(inputValue))
                .withIncluded(getIncluded(inputValue));

        if (jsonApiConfiguration.isJsonApiVersionRendered()) {
            doc = doc.withJsonapi(new JsonApiJsonApi());
        }

        if (jsonApiConfiguration.isPageMetaAutomaticallyCreated() && collectionModel instanceof PagedModel) {
            JsonApiModel model =
                    (JsonApiModel) JsonApiModelBuilder.jsonApiModel().model(collectionModel).pageMeta().build();
            Map<String, Object> metaData = model.getMetaData();
            doc = doc.withMeta(metaData);
        }

        if (inputValue instanceof JsonApiModel) {
            // in some cases we want to add the metadata to the top level JSON:API document
            Map<String, Object> metaData = ((JsonApiModel) inputValue).getMetaData();
            if (embeddedMeta != metaData || data == null) {
                if (doc.getMeta() == null) {
                    doc = doc.withMeta(metaData);
                } else {
                    final Map<String, Object> meta = doc.getMeta();
                    // add/override with metadata created with builder
                    // this will override the previous generated page metadata, if the key is the same
                    for (Map.Entry<?, ?> entry : metaData.entrySet()) {
                        meta.put(entry.getKey().toString(), entry.getValue());
                    }
                }
            }
        }

        // issue #13: if meta is set, we don't want to serialize to "data": null
        if (doc.getMeta() != null && !doc.getMeta().isEmpty() && doc.getData() == null) {
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
        return links;
    }

    private @Nullable
    List<JsonApiData> getIncluded(RepresentationModel<?> representationModel) {
        if (representationModel instanceof JsonApiModel) {
            final List<RepresentationModel<?>> includedEntities =
                    ((JsonApiModel) representationModel).getIncludedEntities();
            final CollectionModel<RepresentationModel<?>> collectionModel = CollectionModel.of(includedEntities);
            return JsonApiData.extractCollectionContent(
                    collectionModel, objectMapper, jsonApiConfiguration,
                    ((JsonApiModel) representationModel).getSparseFieldsets(), true);
        }
        return null;
    }
}
