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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

abstract class AbstractJsonApiModelSerializer<T extends RepresentationModel<?>>
        extends AbstractJsonApiSerializer<T> {


    private final JsonApiConfiguration jsonApiConfiguration;

    private static class JsonApiDocumentWithoutSerializedData extends JsonApiDocument {
        JsonApiDocumentWithoutSerializedData(JsonApiDocument jsonApiDocument) {
            this.meta = jsonApiDocument.meta;
            this.links = jsonApiDocument.links;
            this.included = jsonApiDocument.included;
            this.jsonapi = jsonApiDocument.jsonapi;
        }

        @Override
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Object getData() {
            return null;
        }
    }

    protected AbstractJsonApiModelSerializer(Class<?> t, boolean dummy, JsonApiConfiguration jsonApiConfiguration) {
        super(t, dummy);
        this.jsonApiConfiguration = jsonApiConfiguration;
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        CollectionModel<?> collectionModel = null;
        if (value instanceof JsonApiModel) {
            Object content = ((JsonApiModel) value).getContent();
            if (content instanceof CollectionModel) {
                collectionModel = (CollectionModel<?>) content;
            }
        } else if (value instanceof CollectionModel<?>) {
            collectionModel = (CollectionModel<?>) value;
        }

        Object data;
        if (collectionModel != null) {
            data = JsonApiData.extractCollectionContent(collectionModel, jsonApiConfiguration);
        } else {
            final Optional<JsonApiData> jsonApiData = JsonApiData.extractContent(value, true, jsonApiConfiguration);
            data = jsonApiData.orElse(null);
        }

        JsonApiDocument doc = new JsonApiDocument()
                .withData(data)
                .withLinks(getLinksOrNull(value))
                .withIncluded(getIncluded(value));

        if (jsonApiConfiguration.isJsonApiVersionRendered()) {
            doc = doc.withJsonapi(new JsonApiJsonApi());
        }

        if (collectionModel instanceof PagedModel) {
            JsonApiModel model =
                    (JsonApiModel) JsonApiModelBuilder.jsonApiModel().model(collectionModel).pageMeta().build();
            Map<String, Object> metaData = model.getMetaData();
            doc = doc.withMeta(metaData);
        }

        if (value instanceof JsonApiModel) {
            // we want to add the metadata to the top level JSON:API document
            Map<String, Object> metaData = ((JsonApiModel) value).getMetaData();
            if (doc.getMeta() == null) {
                doc = doc.withMeta(metaData);
            } else {
                final Map<String, Object> meta = doc.getMeta();
                // add/override with meta data created with builder
                // this will override the previous generated page meta data, if the key is the same
                for (Map.Entry<?, ?> entry : metaData.entrySet()) {
                    meta.put(entry.getKey().toString(), entry.getValue());
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

    Links getLinksOrNull(RepresentationModel<?> representationModel) {
        Links links = representationModel.getLinks();
        if (links.isEmpty()) {
            links = null;
        }
        return links;
    }

    private List<JsonApiData> getIncluded(RepresentationModel<?> representationModel) {
        if (representationModel instanceof JsonApiModel) {
            final List<RepresentationModel<?>> includedEntities = ((JsonApiModel) representationModel).getIncludedEntities();
            final CollectionModel<RepresentationModel<?>> collectionModel = CollectionModel.of(includedEntities);
            return JsonApiData.extractCollectionContent(collectionModel, jsonApiConfiguration);
        }
        return null;
    }
}
