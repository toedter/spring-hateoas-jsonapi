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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Getter;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonApiModel extends RepresentationModel<JsonApiModel> {

    private final RepresentationModel<?> entity;

    @JsonIgnore
    @Getter
    private final Map<String, JsonApiRelationship> relationships;

    @JsonIgnore
    @Getter
    private final List<RepresentationModel<?>> includedEntities;

    @JsonIgnore
    @Getter
    private Map<String, Object> metaData;

    @JsonIgnore
    @Getter
    private final HashMap<String, Collection<String>> sparseFieldsets;

    JsonApiModel(
            @Nullable RepresentationModel<?> entity,
            @Nullable Map<String, JsonApiRelationship> relationships,
            @Nullable List<RepresentationModel<?>> includedEntities,
            @Nullable Map<String, Object> metadata,
            @Nullable Links links,
            @Nullable HashMap<String, Collection<String>> sparseFieldsets) {

        this.entity = entity;
        this.relationships = relationships;
        this.includedEntities = includedEntities;
        this.metaData = metadata;
        this.sparseFieldsets = sparseFieldsets;

        if (links != null) {
            add(links);
        }
    }

    @Nullable
    @JsonUnwrapped
    public RepresentationModel<?> getContent() {
        return entity;
    }
}
