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

import java.io.IOException;

class JsonApiCollectionModelSerializer extends AbstractJsonApiSerializer<CollectionModel<?>> {
    public JsonApiCollectionModelSerializer() {
        super(CollectionModel.class, false);
    }

    @Override
    public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        JsonApiDocument doc = null;
        final JsonStreamContext outputContext = gen.getOutputContext();
        if (outputContext.inRoot()) {
            doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(getLinksOrNull(value));

        } else {
            doc = new JsonApiDocument()
                    .withData(JsonApiData.extractCollectionContent(value));
        }

        provider
                .findValueSerializer(JsonApiDocument.class)
                .serialize(doc, gen, provider);
    }
}
