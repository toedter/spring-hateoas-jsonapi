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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.support.Director;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import org.junit.jupiter.api.*;
import org.springframework.hateoas.*;

import java.util.ArrayList;
import java.util.List;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.junit.jupiter.api.Assertions.assertThrows;
// end::import-builder[]

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiModelBuilder Test")
class JsonApiModelBuilderIntegrationTest extends AbstractJsonApiTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration(null, null);
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper, new JsonApiConfiguration());
    }

    @Test
    void should_build_empty_model() throws Exception {
        final RepresentationModel<?> jsonApiModel = jsonApiModel().build();
        final String emptyDoc = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_build_empty_entity_model() throws Exception {
        final RepresentationModel<?> jsonApiModel = jsonApiModel().model(new Object()).build();
        final String emptyDoc = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_build_empty_model_with_link_object() throws Exception {
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(new Object())
                        .link(Link.of("http://localhost/items").withSelfRel())
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "emptyModelWithSelfLink.json");
    }

    @Test
    void should_build_empty_model_with_link() throws Exception {
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(new Object())
                        .link("http://localhost/items", IanaLinkRelations.SELF)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "emptyModelWithSelfLink.json");
    }

    @Test
    void should_build_single_movie_model() throws Exception {
        // tag::build-movie-model[]
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel = jsonApiModel().model(movie).build();
        // end::build-movie-model[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }


    @Test
    void should_build_single_movie_entity_model() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel = jsonApiModel().model(EntityModel.of(movie)).build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship() throws Exception {
        // tag::build-relationship[]
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(movie)
                        .relationship("directors", EntityModel.of(director))
                        .build();
        // end::build-relationship[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationship.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_and_links() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(movie)
                        .relationship("directors", EntityModel.of(director),
                                "http://movies/1/relationships/1",
                                "http://movies/1/directors/1")
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithLinks.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships() throws Exception {
        Movie movie = new Movie("4", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", EntityModel.of(director2))
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationships.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships_and_included() throws Exception {
        // tag::build-included[]
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .included(director1)
                        .included(director2)
                        .build();
        // end::build-included[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationshipsAndIncluded.json");
    }

    @Test
    void should_build_paged_movie_model_with_many_relationships_and_included() throws Exception {
        // tag::complex-paged-model[]
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        final EntityModel<Director> director1EntityModel = EntityModel.of(director1);
        Director director2 = new Director("2", "Lilly Wachowski");
        final EntityModel<Director> director2EntityModel = EntityModel.of(director2);

        final RepresentationModel<?> jsonApiModel1 =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1EntityModel)
                        .relationship("directors", director2EntityModel)
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        Movie movie2 = new Movie("3", "Star Wars");
        Director director3 = new Director("3", "George Lucas");
        final EntityModel<Director> director3EntityModel = EntityModel.of(director3);

        final RepresentationModel<?> jsonApiModel2 =
                jsonApiModel()
                        .model(movie2)
                        .relationship("directors", director3EntityModel)
                        .build();

        List<RepresentationModel<?>> movies = new ArrayList<>();
        movies.add(jsonApiModel1);
        movies.add(jsonApiModel2);

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(2, 1, 100, 50);
        Link selfLink = Link.of("http://localhost/movies").withSelfRel();
        final PagedModel<RepresentationModel<?>> pagedModel = PagedModel.of(movies, pageMetadata, selfLink);

        RepresentationModel<?> pagedJasonApiModel =
                jsonApiModel()
                        .model(pagedModel)
                        .included(director1EntityModel)
                        .included(director2EntityModel)
                        .pageMeta()
                        .pageLinks("http://localhost/movies")
                        .build();
        // end::complex-paged-model[]

        final String pagedModelJson = mapper.writeValueAsString(pagedJasonApiModel);
        compareWithFile(pagedModelJson, "moviesPagedJsonApiModelWithIncluded.json");
    }

    @Test
    void should_build_single_movie_entity_model_with_meta() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(EntityModel.of(movie)).meta("x","y").build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModelWithMeta.json");
    }


    @Test
    void should_not_build_with_second_entity() {
        Movie movie = new Movie("1", "Star Wars");

        assertThrows(IllegalStateException.class, () -> {
            final RepresentationModel<?> jsonApiModel =
                    jsonApiModel().model(movie)
                            .model(movie)
                            .build();
        });
    }

    @Test
    void should_not_build_pagination_meta_with_entity_model_set() {
        Movie movie = new Movie("1", "Star Wars");

        assertThrows(IllegalStateException.class, () -> jsonApiModel()
               .model(movie)
               .pageMeta()
               .build());
    }

    @Test
    void should_not_build_pagination_meta_with_no_paged_model_set() {
        assertThrows(IllegalStateException.class, () -> jsonApiModel()
                .pageMeta()
                .build());
    }

    @Test
    void should_not_build_pagination_meta_with_no_page_meta_data_set() {
        assertThrows(IllegalStateException.class, () -> jsonApiModel()
                .model(PagedModel.empty())
                .pageMeta()
                .build());
    }
}
