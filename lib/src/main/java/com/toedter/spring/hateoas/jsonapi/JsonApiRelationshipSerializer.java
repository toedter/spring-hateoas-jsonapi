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

import java.util.Collection;
import java.util.Map;
import org.springframework.hateoas.Links;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;

class JsonApiRelationshipSerializer extends AbstractJsonApiSerializer<JsonApiRelationship> {

  private final JsonApiConfiguration jsonApiConfiguration;

  public JsonApiRelationshipSerializer(JsonApiConfiguration jsonApiConfiguration) {
    super(JsonApiRelationship.class);
    this.jsonApiConfiguration = jsonApiConfiguration;
  }

  @Override
  public void serialize(
      JsonApiRelationship value, JsonGenerator gen, SerializationContext provider) {
    Object data = value.getData();
    if (data != null) {
      if (data instanceof Collection) {
        data = value.toJsonApiResourceCollection((Collection<?>) data, jsonApiConfiguration);
      } else {
        data = value.toJsonApiResource(data, jsonApiConfiguration);
      }
    }

    // Start writing the relationship object.
    gen.writeStartObject();

    // Handle data field serialization.
    if (value.isDataExplicitlySet()) {
      // When data is explicitly set (even if null), always serialize it.
      gen.writeName("data");
      gen.writePOJO(data);
    } else if (data != null) {
      // When data is not explicitly set but is present, serialize it.
      gen.writeName("data");
      gen.writePOJO(data);
    }
    // If data is not explicitly set and is null, omit the field entirely.

    // Handle links field serialization.
    Links links = value.getLinks();
    if (links != null && !links.isEmpty()) {
      gen.writeName("links");
      gen.writePOJO(links);
    }

    // Handle meta field serialization.
    Map<String, Object> meta = value.getMeta();
    if (meta != null && !meta.isEmpty()) {
      gen.writeName("meta");
      gen.writePOJO(meta);
    }

    gen.writeEndObject();
  }
}
