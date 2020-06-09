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
 * It includes building JSON:API relationships
 *
 * @author Kai Toedter
 */
public class JsonApiDocumentBuilder {
    private RepresentationModel model;
    private Links links = Links.NONE;
    private final HashMap<String, List<JsonApiRelationship>> relationships = new HashMap<>();

    @RequiredArgsConstructor
    static class JsonApiRepresentationModel extends RepresentationModel<JsonApiRepresentationModel> {

        private final RepresentationModel entity;
        private final HashMap<String, List<JsonApiRelationship>> relationships;

        public JsonApiRepresentationModel(
                @Nullable RepresentationModel entity,
                HashMap<String, List<JsonApiRelationship>> relationships,
                Links links) {
            this.entity = entity;
            this.relationships = relationships;
            add(links);
        }

        @Nullable
        public RepresentationModel getContent() {
            return entity;
        }

        public HashMap<String, List<JsonApiRelationship>> getRelationships() {
            return relationships;
        }
    }

    private JsonApiDocumentBuilder() {}

    public JsonApiDocumentBuilder entity(RepresentationModel entity) {

        Assert.notNull(entity, "Entity must not be null!");

        if (model != null) {
            throw new IllegalStateException("Model object already set!");
        }

        this.model = entity;

        return this;
    }

    public JsonApiDocumentBuilder link(Link link) {
        this.links = links.and(link);
        return this;
    }

    public JsonApiDocumentBuilder link(String href, LinkRelation relation) {
        return link(Link.of(href, relation));
    }

    public JsonApiDocumentBuilder links(Iterable<Link> links) {
        this.links = this.links.and(links);

        return this;
    }

    public JsonApiDocumentBuilder relationship(String name, EntityModel<?> entityModel) {
        List<JsonApiRelationship> relationships = this.relationships.computeIfAbsent(name, k -> new ArrayList<>());
        relationships.add(JsonApiRelationship.of(entityModel));
        return this;
    }

    public RepresentationModel<?> build() {
        return new JsonApiRepresentationModel(model, relationships, links);
    }

    public static JsonApiDocumentBuilder jsonApiModel() {
        return new JsonApiDocumentBuilder();
    }
}
