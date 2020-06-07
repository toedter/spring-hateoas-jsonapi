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

import com.fasterxml.jackson.annotation.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Value
@With
@JsonPropertyOrder({"data", "links", "meta"})
public class JsonApiRelationship {
    @Getter
    @With(AccessLevel.PACKAGE)
    List<JsonApiResource> data;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    Links links;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    Map<String, Object> meta;

    @JsonCreator
    JsonApiRelationship(
            @JsonProperty("data") @Nullable List<JsonApiResource> data,
            @JsonProperty("links") @Nullable Links links,
            @JsonProperty("meta") @Nullable Map<String, Object> meta
    ) {
        this.data = data;
        this.links = links;
        this.meta = meta;
    }

    public JsonApiRelationship() {
        this(null, null, null);
    }

    public static JsonApiRelationship of(EntityModel<?> entityModel) {
        final Object content = entityModel.getContent();
        Object id = JsonApiResource.getId(content);
        String type = JsonApiResource.getType(content);
        return new JsonApiRelationship(Collections.singletonList(new JsonApiResource(id, type)), null, null);
    }
}
