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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toedter.spring.hateoas.jsonapi.support.*;
import com.toedter.spring.hateoas.jsonapi.support.polymorphism.PolymorphicRelationEntity;
import com.toedter.spring.hateoas.jsonapi.support.polymorphism.SuperEChild;
import com.toedter.spring.hateoas.jsonapi.support.polymorphism.SuperEChild2;
import com.toedter.spring.hateoas.jsonapi.support.polymorphism.SuperEntity;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpMethod;

import jakarta.persistence.Id;

import java.io.File;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Jackson2JsonApi Integration Test")
class Jackson2JsonApiIntegrationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        mapper = createObjectMapper(new JsonApiConfiguration().withObjectMapperCustomizer(
                        mapper -> {
                            mapper.registerModule(new JavaTimeModule());
                            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
                        }
                )
                .withTypeForClass(MovieDerivedWithTypeForClass.class, "my-movies")
                .withTypeForClass(DirectorWithEmail.class, "directors-with-email")
                .withTypeForClassUsedForDeserialization(true));
    }

    @Test
    void should_serialize_empty_representation_model() throws Exception {
        RepresentationModel<?> representationModel = new RepresentationModel<>();
        String emptyDoc = mapper.writeValueAsString(representationModel);

        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jpa_id() throws Exception {
        @Getter
        class Movie {
            @Id
            private final String myId = "1";
            private final String title = "Star Wars";
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jpa_id_method() throws Exception {
        class Movie {
            @Getter
            private final String title = "Star Wars";

            @Id
            public String retrieveMyId() {
                return "1";
            }
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_method() throws Exception {
        class Movie {
            @Getter
            private final String title = "Star Wars";

            @JsonApiId
            public String getMyId() {
                return "1";
            }
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_method_and_no_field() throws Exception {
        class Movie {
            @Getter
            private final String title = "Star Wars";

            @JsonApiId
            public String getId() {
                return "1";
            }
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_id_methods_and_no_field() throws Exception {
        class Movie {
            @Getter
            private final String title = "Star Wars";

            @Id
            @JsonIgnore
            public String getJPAId() {
                return "2";
            }

            @JsonApiId
            public String getId() {
                return "1";
            }
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id() throws Exception {
        @Getter
        class Movie {
            @JsonApiId
            private final String myId = "1";
            private final String title = "Star Wars";
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_and_jpa_id() throws Exception {
        @Getter
        class Movie {
            @Id
            @JsonIgnore
            private final String jpaId = "2";
            @JsonApiId
            private final String jsonApiId = "1";
            private final String title = "Star Wars";
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_and_type_and_meta_fields() throws Exception {
        String movieJson = mapper.writeValueAsString(
                EntityModel.of(new MovieWithAnnotations("1", "my-movies", "metaValue", "Star Wars")));
        compareWithFile(movieJson, "movieEntityModelWithThreeAnnotations.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_and_type_methods() throws Exception {
        String movieJson = mapper.writeValueAsString(
                EntityModel.of(new MovieWithGetters("1", "Star Wars", "my-movies", "metaValue")));
        compareWithFile(movieJson, "movieEntityModelWithThreeAnnotations.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_type_on_class() throws Exception {
        String movieJson = mapper.writeValueAsString(
                EntityModel.of(new MovieDerivedWithTypeForClass("1", "Star Wars")));
        compareWithFile(movieJson, "movieEntityModelWithAnnotations.json");
    }

    @Test
    void should_serialize_empty_entity_model() throws Exception {
        final EntityModel<Object> representationModel = EntityModel.of(new Object());
        String emptyDoc = mapper.writeValueAsString(representationModel);

        compareWithFile(emptyDoc, "emptyDoc.json");
    }

    @Test
    void should_serialize_single_movie_entity_model() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModelWithLinks.json");
    }

    @Test
    void should_serialize_single_movie_representation_model() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        MovieRepresentationModelWithJsonApiType movieRepresentationModelWithJsonApiType = new MovieRepresentationModelWithJsonApiType(movie);
        String movieJson = mapper.writeValueAsString(movieRepresentationModelWithJsonApiType);

        compareWithFile(movieJson, "movieRepresentationModel.json");
    }

    @Test
    void should_serialize_movie_collection_model() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        Movie movie2 = new Movie("2", "Avengers");
        List<Movie> movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);

        CollectionModel<Movie> collectionModel = CollectionModel.of(movies).add(Links.of(Link.of("http://localhost/movies").withSelfRel()));
        String moviesJson = mapper.writeValueAsString(collectionModel);

        compareWithFile(moviesJson, "moviesCollectionModelFromResources.json");
    }

    @Test
    void should_serialize_movie_collection_model_with_entity_models() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        EntityModel<Movie> movie1Model = EntityModel.of(movie1);
        movie1Model.add(Link.of("http://localhost/movies/1").withSelfRel());
        Movie movie2 = new Movie("2", "Avengers");
        EntityModel<Movie> movie2Model = EntityModel.of(movie2);
        movie2Model.add(Link.of("http://localhost/movies/2").withSelfRel());
        List<EntityModel<Movie>> movies = new ArrayList<>();
        movies.add(movie1Model);
        movies.add(movie2Model);

        CollectionModel<EntityModel<Movie>> collectionModel = CollectionModel.of(movies).add(Links.of(Link.of("http://localhost/movies").withSelfRel()));
        String moviesJson = mapper.writeValueAsString(collectionModel);

        compareWithFile(moviesJson, "moviesCollectionModel.json");
    }

    @Test
    void should_serialize_movie_paged_model_with_automatically_created_page_meta() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        Movie movie2 = new Movie("2", "Avengers");
        List<Movie> movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(2, 1, 2, 2);
        Link nextLink = Link.of("http://localhost/movies?page[number]=2&page[size]=2").withRel(IanaLinkRelations.NEXT);
        final PagedModel<Movie> pagedModel = PagedModel.of(movies, pageMetadata, nextLink);

        String moviesJson = mapper.writeValueAsString(pagedModel);
        compareWithFile(moviesJson, "moviesPagedModel.json");
    }

    @Test
    void should_serialize_movie_paged_model_with_no_page_meta() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        Movie movie2 = new Movie("2", "Avengers");
        List<Movie> movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(2, 1, 2, 2);
        Link nextLink = Link.of("http://localhost/movies?page[number]=2&page[size]=2").withRel(IanaLinkRelations.NEXT);
        final PagedModel<Movie> pagedModel = PagedModel.of(movies, pageMetadata, nextLink);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withPageMetaAutomaticallyCreated(false));

        String moviesJson = mapper.writeValueAsString(pagedModel);
        compareWithFile(moviesJson, "moviesPagedModelWithoutMeta.json");
    }

    @Test
    void should_serialize_single_movie_with_custom_serializer_entity_model() throws Exception {
        MovieWithCustomSerializer movie = new MovieWithCustomSerializer("1", "Star Wars", "TEST");
        EntityModel<MovieWithCustomSerializer> entityModel = EntityModel.of(movie);
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModelWithCustomSerializer.json");
    }

    @Test
    void should_not_serialize_movie_without_id() {
        Assertions.assertThrows(JsonMappingException.class, () -> {
            Movie movie = new Movie(null, "Star Wars");
            EntityModel<Movie> entityModel = EntityModel.of(movie);
            mapper.writeValueAsString(entityModel);
        });
    }

    @Test
    void should_serialize_movie_without_id() throws Exception {
        mapper = createObjectMapper(
                // tag::noIdMarker[]
                new JsonApiConfiguration().withJsonApiIdNotSerializedForValue("doNotSerialize"));
        // end::noIdMarker[]

        // tag::noIdMovie[]
        Movie movie = new Movie("doNotSerialize", "Star Wars");
        // end::noIdMovie[]
        EntityModel<Movie> entityModel = EntityModel.of(movie);
        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithoutId.json");
    }

    @Test
    void should_not_serialize_movie_without_id_and_link() {
        Assertions.assertThrows(JsonMappingException.class, () -> {
            Movie movie = new Movie(null, "Star Wars");
            EntityModel<Movie> entityModel = EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
            mapper.writeValueAsString(entityModel);
        });
    }

    @Test
    void should_serialize_movie_with_templated_link() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel =
                EntityModel.of(movie).add(
                        Links.of(Link.of("http://localhost/directors?{page,size}").withRel("directors")));
        final String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithTemplatedLink.json");
    }

    @Test
    void should_deserialize_single_movie_entity_model() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isEqualTo("1");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/1");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_playtime() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithPlaytime.class);
        File file = new ClassPathResource("movieWithPlaytimeEntityModel.json", getClass()).getFile();
        EntityModel<MovieWithPlaytime> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithPlaytime movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isEqualTo("1");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");
        assertThat(movie.getPlaytime()).isEqualTo(42);
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_one_relationship() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
        File file = new ClassPathResource("postMovieWithOneRelationship.json", getClass()).getFile();
        EntityModel<MovieWithDirectors> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithDirectors movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        List<Director> directors = movie.getDirectors();
        assertThat(directors).hasSize(1);
        assertThat(directors.get(0).getId()).isEqualTo("1");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_one_list_relationship_and_relationship_type() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithTypedDirectors.class);
        File file = new ClassPathResource("postMovieWithOneRelationshipWithType.json", getClass()).getFile();
        EntityModel<MovieWithTypedDirectors> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithTypedDirectors movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        List<DirectorWithType> directors = movie.getDirectors();
        assertThat(directors).hasSize(1);
        assertThat(directors.get(0).getId()).isEqualTo("1");
        assertThat(directors.get(0).getDirectorType()).isEqualTo("director-type");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_one_set_relationship_and_relationship_type() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithTypedDirectorSet.class);
        File file = new ClassPathResource("postMovieWithOneRelationshipWithType.json", getClass()).getFile();
        EntityModel<MovieWithTypedDirectorSet> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithTypedDirectorSet movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        Set<DirectorWithType> directors = movie.getDirectors();
        assertThat(directors).hasSize(1);
        DirectorWithType director = directors.iterator().next();
        assertThat(director.getId()).isEqualTo("1");
        assertThat(director.getDirectorType()).isEqualTo("director-type");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_one_entity_relationship_and_relationship_type() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithSingleTypedDirector.class);
        File file = new ClassPathResource("postMovieWithOneRelationshipWithType.json", getClass()).getFile();
        EntityModel<MovieWithSingleTypedDirector> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithSingleTypedDirector movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        DirectorWithType director = movie.getDirector();
        assertThat(director.getId()).isEqualTo("1");
        assertThat(director.getDirectorType()).isEqualTo("director-type");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_two_relationship_and_different_relationship_types() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithTypedDirectors.class);
        File file = new ClassPathResource("postMovieWithTwoRelationshipsWithDifferentTypes.json", getClass()).getFile();
        EntityModel<MovieWithTypedDirectors> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithTypedDirectors movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        List<DirectorWithType> directors = movie.getDirectors();
        assertThat(directors).hasSize(2);
        assertThat(directors.get(0).getId()).isEqualTo("1");
        assertThat(directors.get(0).getDirectorType()).isEqualTo("director-type-1");
        assertThat(directors.get(1).getId()).isEqualTo("2");
        assertThat(directors.get(1).getDirectorType()).isEqualTo("director-type-2");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_two_relationships() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
        File file = new ClassPathResource("postMovieWithTwoRelationships.json", getClass()).getFile();
        EntityModel<MovieWithDirectors> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithDirectors movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getId()).isNull();
        assertThat(movie.getTitle()).isEqualTo("New Movie");

        List<Director> directors = movie.getDirectors();
        assertThat(directors).hasSize(2);
        assertThat(directors.get(0).getId()).isEqualTo("1");
        assertThat(directors.get(1).getId()).isEqualTo("2");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_field_annotation() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithAnnotations.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<MovieWithAnnotations> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithAnnotations movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getMyId()).isEqualTo("1");
        assertThat(movie.getType()).isEqualTo("movies");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/1");
    }

    @Test
    void should_deserialize_derived_class_with_field_annotation() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithAnnotationsDerived.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<MovieWithAnnotationsDerived> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithAnnotationsDerived movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getMyId()).isEqualTo("1");
        assertThat(movie.getType()).isEqualTo("movies");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/1");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_method_annotation() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithGetters.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<MovieWithGetters> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        MovieWithGetters movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getMyId()).isEqualTo("1");
        assertThat(movie.getMyType()).isEqualTo("movies");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/1");
    }

    @Test
    void should_deserialize_single_movie_representation_model() throws Exception {
        JavaType movieRepresentationModelType =
                mapper.getTypeFactory()
                        .constructParametricType(RepresentationModel.class, MovieRepresentationModelWithJsonApiType.class);
        File file = new ClassPathResource("movieRepresentationModel.json", getClass()).getFile();
        MovieRepresentationModelWithJsonApiType movieRepresentationModelWithJsonApiType = mapper.readValue(file, movieRepresentationModelType);

        assertThat(movieRepresentationModelWithJsonApiType.getId()).isEqualTo("1");
        assertThat(movieRepresentationModelWithJsonApiType.getName()).isEqualTo("Star Wars");
        assertThat(movieRepresentationModelWithJsonApiType.getType()).isEqualTo("movie-type");

        Links links = movieRepresentationModelWithJsonApiType.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/7");
    }

    @Test
    void should_deserialize_movies_collection_model() throws Exception {
        JavaType moviesCollectionModelType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, Movie.class);
        File file = new ClassPathResource("moviesCollectionModel.json", getClass()).getFile();
        CollectionModel<Movie> movieCollectionModel = mapper.readValue(file, moviesCollectionModelType);
        Collection<Movie> movieCollection = movieCollectionModel.getContent();

        final Iterator<Movie> iterator = movieCollection.iterator();
        Movie movie1 = iterator.next();
        assertThat(movie1.getId()).isEqualTo("1");
        assertThat(movie1.getTitle()).isEqualTo("Star Wars");
        Movie movie2 = iterator.next();
        assertThat(movie2.getId()).isEqualTo("2");
        assertThat(movie2.getTitle()).isEqualTo("Avengers");

        Links links = movieCollectionModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies");
    }

    @Test
    void should_deserialize_movies_collection_model_without_links() throws Exception {
        JavaType moviesCollectionModelType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, Movie.class);
        File file = new ClassPathResource("moviesCollectionModelWithoutLinks.json", getClass()).getFile();
        CollectionModel<Movie> movieCollectionModel = mapper.readValue(file, moviesCollectionModelType);
        Collection<Movie> movieCollection = movieCollectionModel.getContent();

        final Iterator<Movie> iterator = movieCollection.iterator();
        Movie movie1 = iterator.next();
        assertThat(movie1.getId()).isEqualTo("1");
        assertThat(movie1.getTitle()).isEqualTo("Star Wars");
        Movie movie2 = iterator.next();
        assertThat(movie2.getId()).isEqualTo("2");
        assertThat(movie2.getTitle()).isEqualTo("Avengers");

        Links links = movieCollectionModel.getLinks();
        assertThat(links.isEmpty()).isTrue();
    }

    @Test
    void should_deserialize_movies_paged_model() throws Exception {
        JavaType moviesPagedModelType =
                mapper.getTypeFactory().constructParametricType(PagedModel.class, Movie.class);
        File file = new ClassPathResource("moviesPagedModel.json", getClass()).getFile();
        PagedModel<Movie> moviePagedModel = mapper.readValue(file, moviesPagedModelType);
        Collection<Movie> movieCollection = moviePagedModel.getContent();

        final Iterator<Movie> iterator = movieCollection.iterator();
        Movie movie1 = iterator.next();
        assertThat(movie1.getId()).isEqualTo("1");
        assertThat(movie1.getTitle()).isEqualTo("Star Wars");
        Movie movie2 = iterator.next();
        assertThat(movie2.getId()).isEqualTo("2");
        assertThat(movie2.getTitle()).isEqualTo("Avengers");
    }

    @Test
    void should_deserialize_movies_paged_model_with_page_meta() throws Exception {
        JavaType moviesPagedModelType =
                mapper.getTypeFactory().constructParametricType(PagedModel.class, Movie.class);
        File file = new ClassPathResource("moviesPagedModel.json", getClass()).getFile();
        PagedModel<Movie> moviePagedModel = mapper.readValue(file, moviesPagedModelType);
        Collection<Movie> movieCollection = moviePagedModel.getContent();

        final Iterator<Movie> iterator = movieCollection.iterator();
        Movie movie1 = iterator.next();
        assertThat(movie1.getId()).isEqualTo("1");
        assertThat(movie1.getTitle()).isEqualTo("Star Wars");
        Movie movie2 = iterator.next();
        assertThat(movie2.getId()).isEqualTo("2");
        assertThat(movie2.getTitle()).isEqualTo("Avengers");

        PagedModel.PageMetadata metadata = moviePagedModel.getMetadata();
        assertThat(metadata.getNumber()).isEqualTo(1);
        assertThat(metadata.getSize()).isEqualTo(2);
        assertThat(metadata.getTotalPages()).isEqualTo(2);
        assertThat(metadata.getTotalElements()).isEqualTo(2);
    }

    @Test
    void should_deserialize_movies_paged_model_with_entity_links() throws Exception {
        JavaType movieEntityModelType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        JavaType moviesPagedModelType =
                mapper.getTypeFactory().constructParametricType(PagedModel.class, movieEntityModelType);

        File file = new ClassPathResource("moviesPagedModelWithEntityLinks.json", getClass()).getFile();
        PagedModel<EntityModel<Movie>> moviePagedModel = mapper.readValue(file, moviesPagedModelType);
        Collection<EntityModel<Movie>> movieCollection = moviePagedModel.getContent();

        final Iterator<EntityModel<Movie>> iterator = movieCollection.iterator();
        EntityModel<Movie> movie1 = iterator.next();
        assertThat(movie1.getContent().getId()).isEqualTo("1");
        assertThat(movie1.getContent().getTitle()).isEqualTo("Star Wars");
        assertThat(movie1.getLink("imdb").get().getHref())
                .isEqualTo("https://www.imdb.com/title/tt0076759/?ref_=ttls_li_tt");

        EntityModel<Movie> movie2 = iterator.next();
        assertThat(movie2.getContent().getId()).isEqualTo("2");
        assertThat(movie2.getContent().getTitle()).isEqualTo("Avengers");
        assertThat(movie2.getLink("imdb").get().getHref())
                .isEqualTo("https://www.imdb.com/title/tt0848228/?ref_=fn_al_tt_1");

        PagedModel.PageMetadata metadata = moviePagedModel.getMetadata();
        assertThat(metadata.getNumber()).isEqualTo(1);
        assertThat(metadata.getSize()).isEqualTo(2);
        assertThat(metadata.getTotalPages()).isEqualTo(2);
        assertThat(metadata.getTotalElements()).isEqualTo(2);
    }

    @Test
    void should_deserialize_movies_collection_model_with_entity_links() throws Exception {
        JavaType movieEntityModelType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        JavaType moviesPagedModelType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, movieEntityModelType);

        File file = new ClassPathResource("moviesPagedModelWithEntityLinks.json", getClass()).getFile();
        CollectionModel<EntityModel<Movie>> movieCollectionModel = mapper.readValue(file, moviesPagedModelType);
        Collection<EntityModel<Movie>> movieCollection = movieCollectionModel.getContent();

        final Iterator<EntityModel<Movie>> iterator = movieCollection.iterator();
        EntityModel<Movie> movie1 = iterator.next();
        assertThat(movie1.getContent().getId()).isEqualTo("1");
        assertThat(movie1.getContent().getTitle()).isEqualTo("Star Wars");
        assertThat(movie1.getLink("imdb").get().getHref())
                .isEqualTo("https://www.imdb.com/title/tt0076759/?ref_=ttls_li_tt");

        EntityModel<Movie> movie2 = iterator.next();
        assertThat(movie2.getContent().getId()).isEqualTo("2");
        assertThat(movie2.getContent().getTitle()).isEqualTo("Avengers");
        assertThat(movie2.getLink("imdb").get().getHref())
                .isEqualTo("https://www.imdb.com/title/tt0848228/?ref_=fn_al_tt_1");
    }

    @Test
    void should_deserialize_empty_model_with_complex_link() throws Exception {
        File file = new ClassPathResource("emptyModelWithComplexLink.json", getClass()).getFile();
        RepresentationModel<?> movieEntityModel = mapper.readValue(file, RepresentationModel.class);

        assertThat(movieEntityModel.getLinks().hasSize(1)).isTrue();
        assertThat(movieEntityModel.getLink("complex").get().isTemplated()).isTrue();
    }

    @Test
    void should_deserialize_number_to_double() throws Exception {
        JavaType withDoubleEntityModelType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithRating.class);
        File file = new ClassPathResource("movieEntityWithNumber.json", getClass()).getFile();
        EntityModel<MovieWithRating> withDoubleModel = mapper.readValue(file, withDoubleEntityModelType);

        assertThat(Objects.requireNonNull(withDoubleModel.getContent()).getRating()).isEqualTo(8.0);
    }

    @Test
    void should_deserialize_entity_model_with_annotated_type_on_class() throws Exception {
        JavaType movieType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        File file = new ClassPathResource("postMovieWithCustomType.json", getClass()).getFile();
        EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieType);

        assertThat(movieEntityModel.getContent()).isInstanceOf(MovieDerivedWithTypeForClass.class);
    }

    @Test
    void should_deserialize_movie_with_polymorphic_directors_relationships() throws Exception {
        JavaType movieType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
        File file = new ClassPathResource("postMovieWithTwoRelationshipsWithPolymorphicTypes.json", getClass()).getFile();
        EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieType);

        assertThat(movieEntityModel.getContent()).isInstanceOf(MovieWithDirectors.class);

        MovieWithDirectors movieWithDirectors = (MovieWithDirectors) movieEntityModel.getContent();
        assert movieWithDirectors != null;
        assertThat(movieWithDirectors.getDirectors().get(0)).isInstanceOf(Director.class);
        assertThat(movieWithDirectors.getDirectors().get(1)).isInstanceOf(DirectorWithEmail.class);
    }

    @Test
    void should_not_deserialize_movie_with_non_polymorphic_type() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JavaType movieType =
                    mapper.getTypeFactory().constructParametricType(EntityModel.class, Director.class);
            File file = new ClassPathResource("postMovieWithCustomType.json", getClass()).getFile();
            EntityModel<Director> movieEntityModel = mapper.readValue(file, movieType);
        });
    }

    @Test
    void should_not_deserialize_movie_with_illegal_polymorphic_relationships() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            JavaType movieType =
                    mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
            File file = new ClassPathResource("postMovieWithTwoRelationshipsWithIllegalPolymorphicTypes.json",
                    getClass()).getFile();
            EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieType);
        });
    }

    @Test
    void should_serialize_movies_with_long_id() throws Exception {
        MovieWithLongId movie = new MovieWithLongId(1, "Star Wars", "long-movies");
        EntityModel<MovieWithLongId> entityModel =
                EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModelWithLongId.json");
    }

    @Test
    void should_serialize_movie_with_complex_link_and_keep_link_meta() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie);
        Link complexLink = Link.of("https://complex-links.org")
                .withHreflang("EN")
                .withName("name")
                .withTitle("title")
                .withType("type")
                .withMedia("media");

        entityModel.add(complexLink);

        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithComplexLinkAndOldMeta.json");
    }
    @Test
    void should_serialize_movie_with_complex_link() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie);
        Link complexLink = Link.of("https://complex-links.org")
                .withHreflang("EN")
                .withName("name")
                .withTitle("title")
                .withType("type")
                .withMedia("media");

        entityModel.add(complexLink);
        mapper = createObjectMapper(new JsonApiConfiguration()
                .withJsonapi11LinkPropertiesRemovedFromLinkMeta(true));

        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithComplexLink.json");
    }

    @Test
    void should_serialize_single_movie_model_with_many_director_links() throws Exception {
        Movie movie = new Movie("4", "The Matrix");

        EntityModel<Movie> movieEntityModel = EntityModel.of(movie);
        movieEntityModel.add(Link.of("http://mymovies.com/directors/1").withRel("directors"));
        Link bigLink = Link.of("http://mymovies.com/directors/2")
                .withRel("directors")
                .withHreflang("hreflang")
                .withMedia("media")
                .withTitle("title")
                .withType("type")
                .withDeprecation("deprecation")
                .withProfile("profile")
                .withName("Lana Wachowski");
        movieEntityModel.add(bigLink);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withJsonapi11LinkPropertiesRemovedFromLinkMeta(true));

        final String movieJson = mapper.writeValueAsString(movieEntityModel);
        compareWithFile(movieJson, "movieEntityModelWithTwoDirectorsLinks.json");
    }

    @Test
    void should_deserialize_single_movie_model_with_many_director_links() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        File file = new ClassPathResource("movieEntityModelWithTwoDirectorsLinks.json", getClass()).getFile();
        EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie movie = movieEntityModel.getContent();
        assertThat(movie).isNotNull();
        assertThat(movie.getId()).isEqualTo("4");
        assertThat(movie.getTitle()).isEqualTo("The Matrix");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSize(2)).isTrue();

        Object[] linksArray = links.stream().toArray();
        Link link1 = (Link) linksArray[0];
        assertThat(link1.getHref()).isEqualTo("http://mymovies.com/directors/1");

        Link link2 = (Link) linksArray[1];
        assertThat(link2.getHreflang()).isEqualTo("hreflang");
        assertThat(link2.getMedia()).isEqualTo("media");
        assertThat(link2.getTitle()).isEqualTo("title");
        assertThat(link2.getType()).isEqualTo("type");
        assertThat(link2.getDeprecation()).isEqualTo("deprecation");
        assertThat(link2.getProfile()).isEqualTo("profile");
        assertThat(link2.getName()).isEqualTo("Lana Wachowski");
    }

    @Test
    void should_render_jsonapi_version() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie);

        mapper = createObjectMapper(new JsonApiConfiguration().withJsonApiVersionRendered(true));

        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithJsonApiVersion.json");
    }

    @Test
    void should_serialize_custom_instant() throws Exception {
        @Getter
        class InstantExample {
            private final String id = "1";
            private final Instant instant;

            InstantExample() {
                instant = Instant.ofEpochSecond(1603465191);
            }
        }
        EntityModel<InstantExample> entityModel = EntityModel.of(new InstantExample());
        String instantJson = mapper.writeValueAsString(entityModel);
        compareWithFile(instantJson, "instantWithCustomConfig.json");
    }

    @Test
    void should_serialize_with_NON_NULL_annotation() throws Exception {
        @Getter
        @JsonInclude(JsonInclude.Include.NON_NULL)
        class NonNullExample {
            private final String id = "1";
            private final String test = null;
        }
        EntityModel<NonNullExample> entityModel = EntityModel.of(new NonNullExample());
        String json = mapper.writeValueAsString(entityModel);
        compareWithFile(json, "nonNullAnnotationExample.json");
    }

    @Test
    void should_deserialize_polymorphic_relationships() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, PolymorphicRelationEntity.class);
        File file = new ClassPathResource("polymorphicRelationships.json", getClass()).getFile();
        EntityModel<PolymorphicRelationEntity> entityModel = mapper.readValue(file, javaType);

        final List<SuperEntity<?>> relation = Objects.requireNonNull(entityModel.getContent()).getRelation();
        assertThat(relation.get(0).getClass()).isEqualTo(SuperEChild.class);
        assertThat(relation.get(1).getClass()).isEqualTo(SuperEChild2.class);
    }

    @Test
    void should_deserialize_class_without_attributes() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithoutAttributes.class);
        File file = new ClassPathResource("movieWithoutAttributes.json", getClass()).getFile();
        EntityModel<MovieWithoutAttributes> entityModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(entityModel.getContent()).getId()).isEqualTo("1");
    }

    @Test
    void should_not_deserialize_movie_with_wrong_annotation() {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieThrowingException.class);

        Assertions.assertThrows(IllegalStateException.class, () -> {
            File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
            mapper.readValue(file, javaType);
        });
    }

    @Test
    void should_serialize_UUID() throws Exception {
        EntityModel<MovieWithUUID> entityModel =
                EntityModel.of(
                        new MovieWithUUID(UUID.fromString("00000000-0001-e240-0000-00002f08ba38"), "Star Wars"));
        String json = mapper.writeValueAsString(entityModel);
        compareWithFile(json, "movieWithUUID.json");
    }

    @Test
    void should_deserialize_UUID() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithUUID.class);
        File file = new ClassPathResource("movieWithUUID.json", getClass()).getFile();
        EntityModel<MovieWithUUID> entityModel = mapper.readValue(file, javaType);
        assertThat(entityModel.getContent().getId()).hasToString("00000000-0001-e240-0000-00002f08ba38");
    }

    @Test
    void should_deserialize_UUID_with_annotation() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithUUIDAnnotation.class);
        File file = new ClassPathResource("movieWithUUID.json", getClass()).getFile();
        EntityModel<MovieWithUUIDAnnotation> entityModel = mapper.readValue(file, javaType);
        assertThat(entityModel.getContent().getMyId()).hasToString("00000000-0001-e240-0000-00002f08ba38");
    }

    @Test
    void should_deserialize_UUID_with_method() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithUUIDMethod.class);
        File file = new ClassPathResource("movieWithUUID.json", getClass()).getFile();
        EntityModel<MovieWithUUIDMethod> entityModel = mapper.readValue(file, javaType);
        assertThat(entityModel.getContent().getMyId()).hasToString("00000000-0001-e240-0000-00002f08ba38");
    }

    @Test
    void should_not_serialize_empty_attributes_when_configured() throws Exception {
        MovieWithoutAttributes movie = new MovieWithoutAttributes();
        movie.setId("1");
        EntityModel<MovieWithoutAttributes> entityModel = EntityModel.of(movie);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withEmptyAttributesObjectSerialized(false)
                .withTypeForClass(MovieWithoutAttributes.class, "movies"));

        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModelWithoutAttributesObject.json");
    }

    @Test
    void should_serialize_non_empty_attributes_when_configured_with_empty_attributes_false() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withEmptyAttributesObjectSerialized(false));

        String movieJson = mapper.writeValueAsString(entityModel);
        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void should_deserialize_movie_with_primitive_long_id() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithLongId.class);
        File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
        EntityModel<MovieWithLongId> entityModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(entityModel.getContent()).getId()).isEqualTo(1);
    }

    @Test
    void should_deserialize_movie_with_long_object_id() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithLongObjectId.class);
        File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
        EntityModel<MovieWithLongObjectId> entityModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(entityModel.getContent()).getId()).isEqualTo(1);
    }

    @Test
    void should_deserialize_movie_with_primitive_int_id() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithIntId.class);
        File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
        EntityModel<MovieWithIntId> entityModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(entityModel.getContent()).getId()).isEqualTo(1);
    }

    @Test
    void should_deserialize_movie_with_integer_object_id() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithIntegerObjectId.class);
        File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
        EntityModel<MovieWithIntegerObjectId> entityModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(entityModel.getContent()).getId()).isEqualTo(1);
    }

    @Test
    void should_serialize_type_attribute_when_type_for_class_is_used() throws Exception {
        @Getter
        @JsonApiTypeForClass(value = "MyClassType")
        class Movie {
            private final Long id = 1L;
            private final String type = "MyObjectType";
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieWithTypeAttribute.json");
    }

    @Test
    void should_deserialize_collection_model_without_links() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, Movie.class);
        File file = new ClassPathResource("moviesCollectionModelWithoutLinks.json", getClass()).getFile();
        CollectionModel<Movie> collectionModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(collectionModel.getContent())).hasSize(2);
    }

    @Test
    void should_deserialize_collection_model_of_entity_models_without_links() throws Exception {
        JavaType innerType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, innerType);
        File file = new ClassPathResource("moviesCollectionModelWithoutLinks.json", getClass()).getFile();
        CollectionModel<EntityModel<Movie>> collectionModel = mapper.readValue(file, javaType);
        assertThat(Objects.requireNonNull(collectionModel.getContent())).hasSize(2);
    }

    @Test
    void should_serialize_jsonapimeta_field_annotation() throws Exception {
        @Getter
        class Movie {
            private final Long id = 1L;
            private final String title = "Star Wars";
            @JsonApiMeta
            private final String metaProperty = "metaValue";
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModelWithMeta.json");
    }

    @Test
    void should_serialize_jsonapimeta_method_annotation() throws Exception {
        @Getter
        class Movie {
            private final Long id = 1L;
            private final String title = "Star Wars";

            @JsonApiMeta
            public String getMetaProperty() {
                return "metaValue";
            }
        }

        String movieJson = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(movieJson, "movieEntityModelWithMeta.json");
    }

    @Test
    void should_deserialize_jsonapimeta_field_annotation() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithMetaAnnotation.class);
        File file = new ClassPathResource("movieEntityModelWithMeta.json", getClass()).getFile();
        EntityModel<MovieWithMetaAnnotation> entityModel = mapper.readValue(file, javaType);

        assertThat(entityModel.getContent().getMetaProperty()).isEqualTo("metaValue");
    }

    @Test
    void should_deserialize_jsonapimeta_method_annotation() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithMethodMetaAnnotation.class);
        File file = new ClassPathResource("movieEntityModelWithMeta.json", getClass()).getFile();
        EntityModel<MovieWithMethodMetaAnnotation> entityModel = mapper.readValue(file, javaType);

        assertThat(entityModel.getContent().getMetaProperty()).isEqualTo("metaValue");
    }

    @Test
    void should_serialize_affordance_with_proprietary_format() throws Exception {
        Link link = Affordances.of(Link.of("/"))
                .afford(HttpMethod.POST)
                .withInputAndOutput(Movie.class) //
                .withName("create-movie") //
                .toLink();

        RepresentationModel<?> movieModel = CollectionModel.of(Collections.singletonList(new Movie("1", "New Movie")), link);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withAffordancesRenderedAsLinkMeta(JsonApiConfiguration.AffordanceType.SPRING_HATEOAS));
        String moviesJson = mapper.writeValueAsString(movieModel);
        compareWithFile(moviesJson, "moviesCollectionModelWithAffordances.json");
    }

    @Test
    void should_serialize_affordance_with_hal_forms_format() throws Exception {
        Link link = Affordances.of(Link.of("/"))
                .afford(HttpMethod.POST)
                .withInputAndOutput(Movie.class) //
                .withName("create-movie") //
                .toLink();

        RepresentationModel<?> movieModel = CollectionModel.of(Collections.singletonList(new Movie("1", "New Movie")), link);

        mapper = createObjectMapper(new JsonApiConfiguration()
                .withAffordancesRenderedAsLinkMeta(JsonApiConfiguration.AffordanceType.HAL_FORMS));
        String moviesJson = mapper.writeValueAsString(movieModel);
        compareWithFile(moviesJson, "moviesCollectionModelWithHalFormsAffordances.json");
    }

    @Test
    void should_deserialize_collection_model_of_entity_models_with_relationships_and_included() throws Exception {
        JavaType innerType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(CollectionModel.class, innerType);
        File file = new ClassPathResource("moviesCollectionModelWithIncluded.json", getClass()).getFile();
        CollectionModel<EntityModel<MovieWithDirectors>> collectionModel = mapper.readValue(file, javaType);

        assertThat(Objects.requireNonNull(collectionModel.getContent())).hasSize(2);
        Iterator<EntityModel<MovieWithDirectors>> iterator = collectionModel.getContent().iterator();
        EntityModel<MovieWithDirectors> entityModel = iterator.next();
        Director director = entityModel.getContent().getDirectors().get(0);
        assertThat(director.getId()).isEqualTo("1");
        assertThat(director.getName()).isEqualTo("Lana Wachowski");
        Director director2 = entityModel.getContent().getDirectors().get(1);
        assertThat(director2.getId()).isEqualTo("2");
        assertThat(director2.getName()).isEqualTo("Lilly Wachowski");
        entityModel = iterator.next();
        Director director3 = entityModel.getContent().getDirectors().get(0);
        assertThat(director3.getId()).isEqualTo("3");
        assertThat(director3.getName()).isEqualTo("George Lucas");
    }

    @Test
    void should_deserialize_entity_model_with_relationships_and_included() throws Exception {
        JavaType javaType = mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithDirectors.class);
        File file = new ClassPathResource("movieWithIncludedRelationships.json", getClass()).getFile();
        EntityModel<MovieWithDirectors> entityModel = mapper.readValue(file, javaType);

        Director director = entityModel.getContent().getDirectors().get(0);
        assertThat(director.getId()).isEqualTo("3");
        assertThat(director.getName()).isEqualTo("George Lucas");
    }

    private void compareWithFile(String json, String fileName) throws Exception {
        File file = new ClassPathResource(fileName, getClass()).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonMapper.builder().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JsonNode jsonNode = objectMapper.readValue(file, JsonNode.class);
        assertThat(json).isEqualTo(jsonNode.toString());
    }

    private ObjectMapper createObjectMapper(JsonApiConfiguration jsonApiConfiguration) {
        JsonApiMediaTypeConfiguration configuration =
                new JsonApiMediaTypeConfiguration(null, null);
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper, jsonApiConfiguration);
        return mapper;
    }
}
