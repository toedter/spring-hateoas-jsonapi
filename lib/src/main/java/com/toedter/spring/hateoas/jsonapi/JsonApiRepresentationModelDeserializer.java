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
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.util.Assert;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;

class JsonApiRepresentationModelDeserializer
    extends AbstractJsonApiModelDeserializer<RepresentationModel<?>> {

  public static final String CANNOT_DESERIALIZE_INPUT_TO_REPRESENTATION_MODEL =
      "Cannot deserialize input to RepresentationModel";

  public JsonApiRepresentationModelDeserializer(JsonApiConfiguration jsonApiConfiguration) {
    super(jsonApiConfiguration);
  }

  protected JsonApiRepresentationModelDeserializer(
      JavaType contentType, JsonApiConfiguration jsonApiConfiguration) {
    super(contentType, jsonApiConfiguration);
  }

  @Override
  protected RepresentationModel<?> convertToRepresentationModel(
      List<Object> resources, JsonApiDocument doc) {
    Assert.notNull(doc, "JsonApiDocument must not be null!");
    Links links = doc.getLinks();

    // Handle empty resources (data was null)
    if (resources.isEmpty()) {
      RepresentationModel<?> representationModel = new RepresentationModel<>();
      representationModel.add(links);
      return representationModel;
    }

    if (resources.size() == 1) {
      RepresentationModel<?> representationModel;
      if (resources.get(0) instanceof RepresentationModel<?>) {
        representationModel = (RepresentationModel<?>) resources.get(0);
      } else if (resources.get(0) == null) {
        representationModel = new RepresentationModel<>();
      } else {
        throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_REPRESENTATION_MODEL);
      }
      representationModel.add(links);
      return representationModel;
    }
    throw new IllegalArgumentException(CANNOT_DESERIALIZE_INPUT_TO_REPRESENTATION_MODEL);
  }

  protected ValueDeserializer<?> createJsonDeserializer(JavaType type) {
    return new JsonApiRepresentationModelDeserializer(type, jsonApiConfiguration);
  }
}
