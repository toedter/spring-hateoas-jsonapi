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

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.*;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Builder API to create complex JSON:API representations exposing a JSON:API idiomatic API.
 * It included building JSON:API relatioships and included resources
 *
 * @author Kai Toedter
 */

public class JsonApiResourceModelBuilder {
    private Object model;
    private Links links = Links.NONE;
    private final HashMap<String, List<JsonApiRelationship>> relationships = new HashMap<>();

    @RequiredArgsConstructor
    static class JsonApiRepresentationModel<T> extends EntityModel<T> {

        private final T entity;
        private final HashMap<String, List<JsonApiRelationship>> relationships;

        public JsonApiRepresentationModel(
                @Nullable T entity,
                HashMap<String, List<JsonApiRelationship>> relationships,
                Links links) {
            this.entity = entity;
            this.relationships = relationships;
            add(links);
        }

        @Nullable
        @Override
        public T getContent() {
            return entity;
        }

        public HashMap<String, List<JsonApiRelationship>> getRelationships() {
            return relationships;
        }
    }

    public JsonApiResourceModelBuilder entity(Object entity) {

        Assert.notNull(entity, "Entity must not be null!");

        if (model != null) {
            throw new IllegalStateException("Model object already set!");
        }

        this.model = entity;

        return this;
    }

    public JsonApiResourceModelBuilder link(Link link) {
        this.links = links.and(link);
        return this;
    }

    public JsonApiResourceModelBuilder link(String href, LinkRelation relation) {
        return link(Link.of(href, relation));
    }

    public JsonApiResourceModelBuilder links(Iterable<Link> links) {
        this.links = this.links.and(links);

        return this;
    }

    public JsonApiResourceModelBuilder relationship(String name, EntityModel<?> entityModel) {
        List<JsonApiRelationship> relationships = this.relationships.computeIfAbsent(name, k -> new ArrayList<>());
        relationships.add(JsonApiRelationship.of(entityModel));
        return this;
    }

    public RepresentationModel<?> build() {
        return new JsonApiRepresentationModel<>(model, relationships, links);
    }
}
