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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import java.io.IOException;
import java.util.List;

public abstract class AbstractJsonApiModelSerializer<T extends RepresentationModel<?>>
        extends AbstractJsonApiSerializer<T> {

    protected AbstractJsonApiModelSerializer(Class<T> t) {
        super(t);
    }

    protected AbstractJsonApiModelSerializer(Class<?> t, boolean dummy) {
        super(t, dummy);
    }

    @Override
    public void serialize(T value, JsonGenerator gen, SerializerProvider provider) throws IOException {

        JsonApiDocument doc = new JsonApiDocument()
                .withJsonapi(new JsonApiJsonApi())
                .withData(JsonApiData.extractCollectionContent(value))
                .withLinks(getLinksOrNull(value))
                .withIncluded(getIncluded(value));

        if (value instanceof JsonApiModel) {
            // In case the content of an JsonApiRepresentationModel is a PagedModel,
            // we want to add the page metadata to the top level JSON:API document
            final RepresentationModel<?> subcontent = ((JsonApiModel) value).getContent();
            if (subcontent instanceof PagedModel) {
                JsonApiPagedModelSerializer jsonApiPagedModelSerializer = new JsonApiPagedModelSerializer();
                doc = jsonApiPagedModelSerializer.postProcess((PagedModel<?>) subcontent, doc);
            }
        } else {
            doc = postProcess(value, doc);
        }

        provider
                .findValueSerializer(JsonApiDocument.class)
                .serialize(doc, gen, provider);
    }

    protected JsonApiDocument postProcess(T value, JsonApiDocument doc) {
        return doc;
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
            return JsonApiData.extractCollectionContent(collectionModel);
        }
        return null;
    }
}
