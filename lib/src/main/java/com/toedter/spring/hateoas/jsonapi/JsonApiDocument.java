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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.With;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@With
@JsonPropertyOrder({"jsonapi", "data", "included", "links", "meta"})
class JsonApiDocument {

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    @Nullable
    private final JsonApiJsonApi jsonapi;

    @Getter
    @With(AccessLevel.PACKAGE)
    @JsonProperty("data")
    @Nullable
    // data can either be a single JsonApiData object or a list of JsonApiData objects
    private final Object data;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    @Nullable
    private final Map<String, Object> meta;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    @Nullable
    private final JsonApiErrors errors;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    @Nullable
    private final Links links;

    @Getter
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @With(AccessLevel.PACKAGE)
    @Nullable
    private final List<JsonApiData> included;

    @JsonCreator
    JsonApiDocument(@JsonProperty("jsonapi") @Nullable JsonApiJsonApi jsonapi,
                    @JsonProperty("data") @Nullable Object data,
                    @JsonProperty("meta") @Nullable Map<String, Object> meta,
                    @JsonProperty("errors") @Nullable JsonApiErrors errors,
                    @JsonProperty("links") @Nullable Links links,
                    @JsonProperty("included") @Nullable List<JsonApiData> included
    ) {
        this.jsonapi = jsonapi;
        this.data = data;
        this.meta = meta;
        this.errors = errors;
        this.links = links;
        this.included = included;
    }

    public JsonApiDocument() {
        this(null, null, null, null, null, null);
    }
}
