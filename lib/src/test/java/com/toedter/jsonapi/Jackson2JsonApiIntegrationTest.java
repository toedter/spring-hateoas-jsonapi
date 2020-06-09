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

import com.fasterxml.jackson.databind.*;
import com.toedter.jsonapi.support.Movie;
import com.toedter.jsonapi.support.MovieRepresentationModel;
import com.toedter.jsonapi.support.MovieWithLongId;
import com.toedter.spring.hateoas.jsonapi.JsonApiMediaTypeConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Jackson2JsonApi Integration Test")
class Jackson2JsonApiIntegrationTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration();
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper);
    }

    @Test
    void should_serialize_empty_representation_model() throws Exception {
        RepresentationModel<?> representationModel = new RepresentationModel<>();
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
        MovieRepresentationModel movieRepresentationModel = new MovieRepresentationModel(movie);
        String movieJson = mapper.writeValueAsString(movieRepresentationModel);

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
            EntityModel<Movie> entityModel = EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
            mapper.writeValueAsString(entityModel);
        });
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
    void should_deserialize_single_movie_representation_model() throws Exception {
        JavaType movieRepresentationModelType =
                mapper.getTypeFactory()
                        .constructParametricType(RepresentationModel.class, MovieRepresentationModel.class);
        File file = new ClassPathResource("movieRepresentationModel.json", getClass()).getFile();
        MovieRepresentationModel movieRepresentationModel = mapper.readValue(file, movieRepresentationModelType);

        assertThat(movieRepresentationModel.getId()).isEqualTo("1");
        assertThat(movieRepresentationModel.getName()).isEqualTo("Star Wars");
        assertThat(movieRepresentationModel.get_type()).isEqualTo("movie-type");

        Links links = movieRepresentationModel.getLinks();
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

        PagedModel.PageMetadata pageMetadata = moviePagedModel.getMetadata();
        assert pageMetadata != null;
        assertThat(pageMetadata.getSize()).isEqualTo(2);
        assertThat(pageMetadata.getNumber()).isEqualTo(1);
        assertThat(pageMetadata.getTotalPages()).isEqualTo(2);
        assertThat(pageMetadata.getTotalElements()).isEqualTo(2);
    }

    @Test
    void shouldSerializeMoviesWithLongId() throws Exception {
        MovieWithLongId movie = new MovieWithLongId(1, "Star Wars", "long-movies");
        EntityModel<MovieWithLongId> entityModel =
                EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModelWithLongId.json");
    }

    @Test
    void shouldSerializeMovieWithComplexLink() throws Exception {
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

    private void compareWithFile(String json, String fileName) throws Exception {
        File file = new ClassPathResource(fileName, getClass()).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JsonNode jsonNode = objectMapper.readValue(file, JsonNode.class);
        assertThat(json).isEqualTo(jsonNode.toString());
    }
}
