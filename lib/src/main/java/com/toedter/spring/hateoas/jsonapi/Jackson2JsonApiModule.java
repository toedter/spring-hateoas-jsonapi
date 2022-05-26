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

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.hateoas.*;
import org.springframework.lang.Nullable;

/**
 * Jackson {@link SimpleModule} for {@literal JSON:API} serializers and deserializers.
 *
 * @author Kai Toedter
 */
public class Jackson2JsonApiModule extends SimpleModule {

    /**
     * Creates a new {@literal Jackson2JsonApiModule} with a given {@link JsonApiConfiguration}.
     *
     * @param jsonApiConfiguration the {@link JsonApiConfiguration}
     */
    public Jackson2JsonApiModule(@Nullable JsonApiConfiguration jsonApiConfiguration) {

        super("json-api-module",
                new Version(1, 0, 0, null,
                        "com.toedter",
                        "spring-hateoas-jsonapi"));

        setMixInAnnotation(EntityModel.class, EntityModelMixin.class);
        setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
        setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
        setMixInAnnotation(PagedModel.class, PagedModelMixin.class);

        // Links class has no default constructor, so we cannot use a Mixin
        JsonApiLinksSerializer jsonApiLinksSerializer = new JsonApiLinksSerializer();
        if(jsonApiConfiguration != null) {
            jsonApiLinksSerializer.setJsonApiConfiguration(jsonApiConfiguration);
        }
        addSerializer(Links.class, jsonApiLinksSerializer);

        addDeserializer(Links.class, new JsonApiLinksDeserializer());
    }

    /**
     * Creates a new {@literal Jackson2JsonApiModule} without {@link JsonApiConfiguration}.
     */
    public Jackson2JsonApiModule() {
        this(null);
    }

    @JsonSerialize(using = JsonApiEntityModelSerializer.class)
    @JsonDeserialize(using = JsonApiEntityModelDeserializer.class)
    abstract static class EntityModelMixin<T> extends EntityModel<T> {
    }

    @JsonSerialize(using = JsonApiRepresentationModelSerializer.class)
    @JsonDeserialize(using = JsonApiRepresentationModelDeserializer.class)
    abstract static class RepresentationModelMixin extends RepresentationModel<RepresentationModelMixin> {
    }

    @JsonSerialize(using = JsonApiCollectionModelSerializer.class)
    @JsonDeserialize(using = JsonApiCollectionModelDeserializer.class)
    abstract static class CollectionModelMixin<T> extends CollectionModel<T> {
    }

    @JsonSerialize(using = JsonApiPagedModelSerializer.class)
    @JsonDeserialize(using = JsonApiPagedModelDeserializer.class)
    abstract static class PagedModelMixin<T> extends PagedModel<T> {
    }
}
