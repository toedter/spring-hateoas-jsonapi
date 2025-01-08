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
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.Getter;
import org.springframework.hateoas.Links;

class JsonApiRelationshipSerializer
  extends AbstractJsonApiSerializer<JsonApiRelationship> {

  private final transient JsonApiConfiguration jsonApiConfiguration;

  @Getter
  private static class JsonApiRelationshipForSerialization {

    @Nullable
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object data;

    @Nullable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Links links;

    @Nullable
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final Map<String, Object> meta;

    public JsonApiRelationshipForSerialization(
      @Nullable Object data,
      @Nullable Links links,
      @Nullable Map<String, Object> meta
    ) {
      this.data = data;
      this.links = links;
      this.meta = meta;
    }
  }

  @Getter
  private static class JsonApiNullDataRelationshipForSerialization {

    private final Object data = null;
  }

  public JsonApiRelationshipSerializer(
    JsonApiConfiguration jsonApiConfiguration
  ) {
    super(JsonApiRelationship.class, false);
    this.jsonApiConfiguration = jsonApiConfiguration;
  }

  @Override
  public void serialize(
    JsonApiRelationship value,
    JsonGenerator gen,
    SerializerProvider provider
  ) throws IOException {
    Object data = value.getData();
    if (data != null) {
      if (data instanceof Collection) {
        data = value.toJsonApiResourceCollection(
          (Collection<?>) data,
          jsonApiConfiguration
        );
      } else {
        data = value.toJsonApiResource(data, jsonApiConfiguration);
      }
    }

    if (data == null && value.getLinks() == null && value.getMeta() == null) {
      provider
        .findValueSerializer(JsonApiNullDataRelationshipForSerialization.class)
        .serialize(
          new JsonApiNullDataRelationshipForSerialization(),
          gen,
          provider
        );
    } else {
      JsonApiRelationshipForSerialization jsonApiRelationship =
        new JsonApiRelationshipForSerialization(
          data,
          value.getLinks(),
          value.getMeta()
        );

      provider
        .findValueSerializer(JsonApiRelationshipForSerialization.class)
        .serialize(jsonApiRelationship, gen, provider);
    }
  }
}
