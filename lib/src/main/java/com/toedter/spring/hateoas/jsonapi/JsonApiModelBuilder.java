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

import org.springframework.hateoas.*;
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
public class JsonApiModelBuilder {
    private RepresentationModel<?> model;
    private Links links = Links.NONE;
    private final HashMap<String, List<JsonApiRelationship>> relationships = new HashMap<>();
    private final List<RepresentationModel<?>> included = new ArrayList<>();

    private JsonApiModelBuilder() {
    }

    public JsonApiModelBuilder entity(RepresentationModel<?> entity) {

        Assert.notNull(entity, "Entity must not be null!");

        if (model != null) {
            throw new IllegalStateException("Model object already set!");
        }

        this.model = entity;

        this.links(entity.getLinks());

        return this;
    }

    public JsonApiModelBuilder entity(Object object) {
        return this.entity(EntityModel.of(object));
    }

    public JsonApiModelBuilder link(Link link) {
        this.links = links.and(link);
        return this;
    }

    public JsonApiModelBuilder link(String href, LinkRelation relation) {
        return link(Link.of(href, relation));
    }

    public JsonApiModelBuilder links(Iterable<Link> links) {
        this.links = this.links.and(links);

        return this;
    }

    public JsonApiModelBuilder relationship(String name, EntityModel<?> entityModel) {
        List<JsonApiRelationship> relationships = this.relationships.computeIfAbsent(name, k -> new ArrayList<>());
        relationships.add(JsonApiRelationship.of(entityModel));
        return this;
    }

    public JsonApiModelBuilder included(EntityModel<?> entityModel) {
        included.add(entityModel);
        return this;
    }

    public RepresentationModel<?> build() {
        return new JsonApiModel(model, relationships, included, links);
    }

    public static JsonApiModelBuilder jsonApiModel() {
        return new JsonApiModelBuilder();
    }
}
