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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.support.Director;
import com.toedter.spring.hateoas.jsonapi.support.DirectorWithMovies;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithRating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiModelBuilder Integration Test")
@SuppressWarnings({"squid:S2699", "squid:S5778"})
class JsonApiModelBuilderIntegrationTest extends JsonApiTestBase {
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
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director)
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
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", EntityModel.of(director),
                                "http://movies/1/relationships/1",
                                "http://movies/1/directors/1")
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithLinks.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_links_and_meta() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        HashMap<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director)
                        .relationship("directors",
                                "http://movies/1/relationships/1",
                                "http://movies/1/directors/1", Links.NONE)
                        .relationship("directors", meta)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithLinksAndMeta.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_links_and_meta_and_different_order() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        HashMap<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors",
                                "http://movies/1/relationships/1",
                                "http://movies/1/directors/1", Links.NONE)
                        .relationship("directors", meta)
                        .relationship("directors", director)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithLinksAndMeta.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_links_and_meta_as_first_element() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        HashMap<String, Object> meta = new HashMap<>();
        meta.put("key", "value");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", meta)
                        .relationship("directors",
                                "http://movies/1/relationships/1",
                                "http://movies/1/directors/1", Links.NONE)
                        .relationship("directors", director)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithLinksAndMeta.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships() throws Exception {
        Movie movie = new Movie("4", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", EntityModel.of(director2))
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationships.json");
    }

    @Test
    void should_build_single_movie_model_with_three_director_relationships() throws Exception {
        Movie movie = new Movie("4", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");
        Director director3 = new Director("3", "A Secret Director");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", EntityModel.of(director2))
                        .relationship("directors", EntityModel.of(director3))
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModelWithThreeDirectorRelationships.json");
    }

    @Test
    void should_build_single_movie_model_with_many_relationships_and_included() throws Exception {
        // tag::build-included[]
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", relatedMovie)
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
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel1 =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", EntityModel.of(relatedMovie))
                        .build();

        Movie movie2 = new Movie("3", "Star Wars");
        Director director3 = new Director("3", "George Lucas");

        final RepresentationModel<?> jsonApiModel2 =
                jsonApiModel()
                        .model(movie2)
                        .relationship("directors", director3)
                        .build();

        List<RepresentationModel<?>> movies = new ArrayList<>();
        movies.add(jsonApiModel1);
        movies.add(jsonApiModel2);

        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(10, 1, 100, 10);
        Link selfLink = Link.of("http://localhost/movies").withSelfRel();
        final PagedModel<RepresentationModel<?>> pagedModel = PagedModel.of(movies, pageMetadata, selfLink);

        RepresentationModel<?> pagedJasonApiModel =
                jsonApiModel()
                        .model(pagedModel)
                        .included(director1)
                        .included(director2)
                        .included(director3)
                        .pageMeta()
                        .pageLinks("http://localhost/movies")
                        .build();
        // end::complex-paged-model[]

        final String pagedModelJson = mapper.writeValueAsString(pagedJasonApiModel);
        compareWithFile(pagedModelJson, "moviesPagedJsonApiModelWithIncluded.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_with_only_meta() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Map<String, Object> meta = new HashMap<>();
        meta.put("meta-key", "meta-value");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", meta)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithOnlyMeta.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_with_only_link() throws Exception {
        Movie movie = new Movie("1", "Star Wars");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", "http://movies.com/1", null, null)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithRelationshipWithOnlyLink.json");
    }


    @Test
    void should_build_paged_movie_with_parametrized_page_links() throws Exception {
        PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(10, 1, 100, 10);
        Link selfLink = Link.of("http://localhost/movies").withSelfRel();
        final PagedModel<RepresentationModel<?>> pagedModel = PagedModel.of(new ArrayList<>(), pageMetadata, selfLink);

        RepresentationModel<?> pagedJasonApiModel =
                jsonApiModel()
                        .model(pagedModel)
                        .pageMeta()
                        .pageLinks("http://localhost/movies?director=lucas")
                        .build();

        final String pagedModelJson = mapper.writeValueAsString(pagedJasonApiModel);

        compareWithFile(pagedModelJson, "pagedJsonApiModelWithPageLinksParameters.json");
    }

    @Test
    void should_build_single_movie_entity_model_with_meta() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().model(EntityModel.of(movie)).meta("metaProperty", "metaValue").build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieEntityModelWithMeta.json");
    }

    @Test
        // issue: #13
    void should_build_with_meta_only() throws Exception {
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel().meta("x", "y").build();

        final String json = mapper.writeValueAsString(jsonApiModel);
        assertThat(json).isEqualTo("{\"meta\":{\"x\":\"y\"}}");
    }

    @Test
    void should_build_single_movie_with_single_collection_relationship_before_data_is_added() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("3", "George Lucas");

        // tag::single-collection-relationship[]
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationshipWithDataArray("directors")
                        .relationship("directors", director)
                        .build();
        // end::single-collection-relationship[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithSingleCollectionRelationship.json");
    }

    @Test
    void should_build_single_movie_with_single_collection_relationship_after_data_is_added() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("3", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationship("directors", director)
                        .relationshipWithDataArray("directors")
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithSingleCollectionRelationship.json");
    }

    @Test
    void should_build_single_movie_with_single_one_element_collection_relationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("3", "George Lucas");

        // tag::single-collection-relationship2[]
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationship("directors", Collections.singletonList(director))
                        .build();
        // end::single-collection-relationship2[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithSingleCollectionRelationship.json");
    }

    @Test
    void should_build_single_movie_with_two_one_element_collection_relationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("3", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationship("directors", Collections.singletonList(director))
                        .relationship("directors", Collections.singletonList(director))
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithSingleCollectionRelationship.json");
    }

    @Test
    void should_build_single_movie_with_empty_collection_relationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationshipWithDataArray("directors")
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithEmptyCollectionRelationship.json");
    }

    @Test
    void should_build_single_movie_with_empty_collection_data_relationship() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .relationship("directors", Collections.EMPTY_LIST)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithEmptyCollectionRelationship.json");
    }

    @Test
    void should_not_build_with_second_entity() {
        Movie movie = new Movie("1", "Star Wars");

        assertThrows(IllegalStateException.class, () -> jsonApiModel()
                .model(movie)
                .model(movie)
                .build());
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

    @Test
    void should_not_build_pagination_links_with_invalid_link_base() {
        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(2, 1, 100, 50);
        assertThrows(IllegalArgumentException.class, () -> jsonApiModel()
                .model(PagedModel.of(Collections.EMPTY_LIST, pageMetadata))
                .pageMeta()
                .pageLinks("httpx://test::8080")
                .build());
    }

    @Test
    void should_not_build_with_invalid_null_value_relationship() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null, null);
        assertThrows(IllegalStateException.class, () -> jsonApiModel()
                .relationship("directors", jsonApiRelationship)
                .build());
    }

    @Test
    void should_not_build_with_invalid_relationship_data_object() {
        Object object = new Object();
        assertThrows(IllegalStateException.class, () -> jsonApiModel()
                .relationship("directors", object)
                .build());
    }

    @Test
    void should_not_add_invalid_relationship_data_object() {
        assertThrows(IllegalArgumentException.class, () -> jsonApiModel()
                .relationship("directors", (EntityModel<?>) null, null, null)
                .build());
    }

    @Test
    void should_not_add_invalid_relationship_links() {
        assertThrows(IllegalArgumentException.class, () -> jsonApiModel()
                .relationship("directors", (String) null, null, null)
                .build());
    }

    @Test
    void should_not_add_invalid_relationship_meta() {
        assertThrows(IllegalArgumentException.class, () -> jsonApiModel()
                .relationship("directors", (HashMap<?, ?>) null)
                .build());
    }

    @Test
    void should_apply_sparse_fieldsets_on_entity_model() throws Exception {
        MovieWithRating movie = new MovieWithRating("1", "Star Wars", 8.6);

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(movie))
                        .fields("movies", "rating")
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithRating.json");
    }

    @Test
    void should_apply_sparse_fieldsets_on_included_resources() throws Exception {
        // tag::sparse-fieldset[]
        MovieWithRating movie = new MovieWithRating("1", "Star Wars", 8.6);
        DirectorWithMovies director = new DirectorWithMovies("3", "George Lucas", 1944);
        director.setMovies(Collections.singletonList(movie));

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(EntityModel.of(director))
                        .fields("directors", "name")
                        .fields("movies", "title")
                        .relationship("movies", movie)
                        .included(movie)
                        .build();
        // end::sparse-fieldset[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "directorWithSparseFieldsetOnIncluded.json");
    }

    @Test
    void should_build_single_movie_model_with_relationship_included_type_config() throws Exception {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration(null, null);
        ObjectMapper mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper,
                new JsonApiConfiguration()
                        .withPluralizedTypeRendered(false)
                        .withLowerCasedTypeRendered(false));

        Movie movie = new Movie("1", "Star Wars");
        Director director = new Director("1", "George Lucas");
        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director)
                        .included(director)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWidthDirectorRelationshipAndTypeConfiguration.json");
    }

    @Test
    void should_build_single_movie_with_different_meta_in_relationship_resources() throws Exception {
        // tag::nesting[]
        Director director = new Director("3", "George Lucas");
        final RepresentationModel<?> directorModel =
                jsonApiModel()
                        .model(EntityModel.of(director))
                        .meta("director-meta", "director-meta-value")
                        .build();

        Map<String, Object> relationshipMeta = new HashMap<>();
        relationshipMeta.put("relationship-meta", "relationship-meta-value");

        Map<String, Object> directorRelationshipMeta = new HashMap<>();
        directorRelationshipMeta.put("director-relationship-meta", "director-relationship-meta-value");

        Movie movie = new Movie("1", "Star Wars");
        final RepresentationModel<?> movieModel =
                jsonApiModel()
                        .model(movie)
                        .meta("movie-meta", "movie-meta-value")
                        .relationship("directors", director, directorRelationshipMeta)
                        .relationship("directors", relationshipMeta)
                        .build();

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movieModel)
                        .meta("top-level-meta", "top-level-meta-value")
                        .included(directorModel)
                        .build();
        // end::nesting[]

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieWithAllMetaLevels.json");
    }

    @Test
    void should_not_include_same_entity_twice() throws Exception {
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", relatedMovie)
                        .included(director1)
                        .included(director2)
                        .included(director2)
                        .included(director1)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationshipsAndIncluded.json");
    }

    @Test
    void should_not_include_same_entity_twice_when_added_as_collection() throws Exception {
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");
        List<Director> directors = new ArrayList<>();
        directors.add(director1);
        directors.add(director1);
        directors.add(director2);
        directors.add(director2);

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", relatedMovie)
                        .included(directors)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationshipsAndIncluded.json");
    }

    @Test
    void should_not_include_same_entity_twice_when_added_as_collection_of_entity_models() throws Exception {
        Movie movie = new Movie("1", "The Matrix");
        Movie relatedMovie = new Movie("2", "The Matrix 2");
        Director director1 = new Director("1", "Lana Wachowski");
        Director director2 = new Director("2", "Lilly Wachowski");
        List<EntityModel<Director>> directors = new ArrayList<>();
        directors.add(EntityModel.of(director1));
        directors.add(EntityModel.of(director1));
        directors.add(EntityModel.of(director2));
        directors.add(EntityModel.of(director2));

        final RepresentationModel<?> jsonApiModel =
                jsonApiModel()
                        .model(movie)
                        .relationship("directors", director1)
                        .relationship("directors", director2)
                        .relationship("relatedMovies", relatedMovie)
                        .included(directors)
                        .build();

        final String movieJson = mapper.writeValueAsString(jsonApiModel);
        compareWithFile(movieJson, "movieJsonApiModelWithManyRelationshipsAndIncluded.json");
    }

}
