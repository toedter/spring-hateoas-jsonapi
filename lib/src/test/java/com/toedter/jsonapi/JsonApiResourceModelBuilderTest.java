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

package com.toedter.jsonapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.jsonapi.support.Director;
import com.toedter.jsonapi.support.Movie;
import com.toedter.spring.hateoas.jsonapi.JsonApiMediaTypeConfiguration;
import com.toedter.spring.hateoas.jsonapi.JsonApiResourceModelBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

public class JsonApiResourceModelBuilderTest extends AbstractJsonApiTest {
    private JsonApiResourceModelBuilder builder;
    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        builder = new JsonApiResourceModelBuilder();
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration();
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper);
    }

    @Test
    void shouldBuildEmptyModel() throws Exception {
        final RepresentationModel<?> jsonApiModel = builder.build();
        final String emptyDoc = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void shouldBuildSingleMovieModel() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel = builder.entity(movie).build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void shouldBuildSingleMovieModelWithRelationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                builder.entity(movie)
                        .relationship("directors", EntityModel.of(director))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationship.json");
    }
}
