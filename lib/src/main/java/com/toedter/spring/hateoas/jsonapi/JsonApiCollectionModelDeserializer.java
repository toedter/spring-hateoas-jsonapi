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

import java.util.List;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Links;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

class JsonApiCollectionModelDeserializer
    extends AbstractJsonApiModelDeserializer<CollectionModel<?>> {

  JsonApiCollectionModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
    super(jsonApiConfiguration);
  }

  protected JsonApiCollectionModelDeserializer(
      JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
    super(contentType, jsonApiConfiguration);
  }

  @Override
  protected CollectionModel<?> convertToRepresentationModel(
      List<Object> resources, JsonApiDocument doc) {
    Links links = doc.getLinks();
    if (links == null) {
      return CollectionModel.of(resources);
    }
    return CollectionModel.of(resources, links);
  }

  protected ValueDeserializer<?> createJsonDeserializer(JavaType type) {
    return new JsonApiCollectionModelDeserializer(type, jsonApiConfiguration);
  }
}
