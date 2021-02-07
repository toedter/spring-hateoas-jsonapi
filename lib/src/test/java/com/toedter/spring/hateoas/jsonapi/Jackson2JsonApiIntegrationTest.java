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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toedter.spring.hateoas.jsonapi.support.Director;
import com.toedter.spring.hateoas.jsonapi.support.DirectorWithType;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import com.toedter.spring.hateoas.jsonapi.support.Movie2;
import com.toedter.spring.hateoas.jsonapi.support.Movie3;
import com.toedter.spring.hateoas.jsonapi.support.Movie4;
import com.toedter.spring.hateoas.jsonapi.support.Movie5;
import com.toedter.spring.hateoas.jsonapi.support.MovieRepresentationModelWithJsonApiType;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithDirectors;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithLongId;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithPlaytime;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithRating;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithSingleTypedDirector;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithTypedDirectorSet;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithTypedDirectors;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithoutAttributes;
import com.toedter.spring.hateoas.jsonapi.support.polymorphy.PolymorphicRelationEntity;
import com.toedter.spring.hateoas.jsonapi.support.polymorphy.SuperEChild;
import com.toedter.spring.hateoas.jsonapi.support.polymorphy.SuperEChild2;
import com.toedter.spring.hateoas.jsonapi.support.polymorphy.SuperEntity;
import lombok.Getter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;

import javax.persistence.Id;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Jackson2JsonApi Integration Test")
class Jackson2JsonApiIntegrationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        mapper = createObjectMapper(new JsonApiConfiguration());
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

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jpa_id_method() throws Exception {
        class Movie {
            @Getter
            private final String title = "Star Wars";

            @Id
            public String getMyId() {
                return "1";
            }
        }

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
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

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
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

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
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

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id() throws Exception {
        @Getter
        class Movie {
            @JsonApiId
            private final String myId = "1";
            private final String title = "Star Wars";
        }

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
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

        String jsonMovie = mapper.writeValueAsString(EntityModel.of(new Movie()));
        compareWithFile(jsonMovie, "movieEntityModel.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_jsonapi_id_and_type() throws Exception {
        String jsonMovie = mapper.writeValueAsString(
                EntityModel.of(new Movie2("1", "Star Wars", "my-movies")));
        compareWithFile(jsonMovie, "movieEntityModelWithAnnotations.json");
    }

    @Test
    void should_serialize_entity_model_with_annotated_type_on_class() throws Exception {
        String jsonMovie = mapper.writeValueAsString(
                EntityModel.of(new Movie5("1", "Star Wars")));
        compareWithFile(jsonMovie, "movieEntityModelWithAnnotations.json");
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
    void should_serialize_movie_paged_model() throws Exception {
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
    void should_not_serialize_movie_without_id() {
        Assertions.assertThrows(JsonMappingException.class, () -> {
            Movie movie = new Movie(null, "Star Wars");
            EntityModel<Movie> entityModel = EntityModel.of(movie);
            mapper.writeValueAsString(entityModel);
        });
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
        assertThat(directors.size()).isEqualTo(1);
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
        assertThat(directors.size()).isEqualTo(1);
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
        assertThat(directors.size()).isEqualTo(1);
//        assertThat(directors.get(0).getId()).isEqualTo("1");
//        assertThat(directors.get(0).getDirectorType()).isEqualTo("director-type");
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
        assertThat(directors.size()).isEqualTo(2);
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
        assertThat(directors.size()).isEqualTo(2);
        assertThat(directors.get(0).getId()).isEqualTo("1");
        assertThat(directors.get(1).getId()).isEqualTo("2");
    }

    @Test
    void should_deserialize_single_movie_entity_model_with_field_annotation() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie2.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<Movie2> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie2 movie = movieEntityModel.getContent();
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
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie4.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<Movie4> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie4 movie = movieEntityModel.getContent();
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
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie3.class);
        File file = new ClassPathResource("movieEntityModelWithLinks.json", getClass()).getFile();
        EntityModel<Movie3> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie3 movie = movieEntityModel.getContent();
        assert movie != null;
        assertThat(movie.getMyId()).isEqualTo("1");
        assertThat(movie.getType()).isEqualTo("movies");
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
    void should_serialize_movies_with_long_id() throws Exception {
        MovieWithLongId movie = new MovieWithLongId(1, "Star Wars", "long-movies");
        EntityModel<MovieWithLongId> entityModel =
                EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModelWithLongId.json");
    }

    @Test
    void should_serialize_movie_with_complex_link() throws Exception {
        MovieWithLongId movie = new MovieWithLongId(1, "Star Wars", "long-movies");
        EntityModel<MovieWithLongId> entityModel =
                EntityModel.of(movie)
                        .add(Links.of(Link.of("http://localhost/movies/1")
                                .withRel("related")
                                .withName("link name")
                                .withTitle("link title")));
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
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
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

        final List<SuperEntity> relation = entityModel.getContent().getRelation();
        assertThat(relation.get(0).getClass()).isEqualTo(SuperEChild.class);
        assertThat(relation.get(1).getClass()).isEqualTo(SuperEChild2.class);
    }

    @Test
    void should_deserialize_class_without_attributes() throws Exception {
        JavaType javaType =
                mapper.getTypeFactory().constructParametricType(EntityModel.class, MovieWithoutAttributes.class);
        File file = new ClassPathResource("movieWithoutAttributes.json", getClass()).getFile();
        EntityModel<MovieWithoutAttributes> entityModel = mapper.readValue(file, javaType);
        assertThat(entityModel.getContent().getId()).isEqualTo("1");
    }

    private void compareWithFile(String json, String fileName) throws Exception {
        File file = new ClassPathResource(fileName, getClass()).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
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
