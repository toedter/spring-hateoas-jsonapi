/*
 * Copyright 2015-2020 the original author or authors.
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
package com.toedter.jsonapi.example;

import com.fasterxml.jackson.databind.*;
import com.toedter.spring.hateoas.jsonapi.Jackson2JsonApiModule;
import com.toedter.spring.hateoas.jsonapi.JsonApiMediaTypeConfiguration;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.core.Relation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

class Jackson2JsonApiIntegrationTest {

    private ObjectMapper mapper;

    @Getter
    @Setter
    static public class MovieRepresentationModel extends RepresentationModel<MovieRepresentationModel> {

        private String id;
        private String type;
        private String name;

        public MovieRepresentationModel() {
        }

        public MovieRepresentationModel(Movie movie) {
            this.id = movie.getId();
            this.name = movie.getTitle();
            this.type = "movie-type";
            add(Links.of(Link.of("http://localhost/movies/7").withSelfRel()));
        }
    }

    @BeforeEach
    void setUpModule() {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration();
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper);
    }

    @Test
    void shouldRenderSingleMovieEntityModel() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        EntityModel<Movie> entityModel = EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
        String movieJson = mapper.writeValueAsString(entityModel);

        compareWithFile(movieJson, "movieEntityModel.json");
    }

    @Test
    void shouldRenderSingleMovieRepresentationModel() throws Exception {
        Movie movie = new Movie("1", "Star Wars");
        MovieRepresentationModel movieRepresentationModel = new MovieRepresentationModel(movie);
        String movieJson = mapper.writeValueAsString(movieRepresentationModel);

        compareWithFile(movieJson, "movieRepresentationModel.json");
    }

    @Test
    void shouldRenderMovieCollectionModel() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        Movie movie2 = new Movie("2", "Avengers");
        List<Movie> movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);

        CollectionModel<Movie> collectionModel = CollectionModel.of(movies).add(Links.of(Link.of("http://localhost/movies").withSelfRel()));
        String moviesJson = mapper.writeValueAsString(collectionModel);

        compareWithFile(moviesJson, "moviesCollectionModel.json");
    }

    @Test
    void shouldRenderMoviePagedModel() throws Exception {
        Movie movie1 = new Movie("1", "Star Wars");
        Movie movie2 = new Movie("2", "Avengers");
        List<Movie> movies = new ArrayList<>();
        movies.add(movie1);
        movies.add(movie2);

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(2, 1, 2, 2);
        final PagedModel<Movie> pagedModel = PagedModel.of(movies, pageMetadata);

        Link nextLink = Link.of("http://localhost/movies?page[number]=2&page[size]=2").withRel(IanaLinkRelations.NEXT);
        pagedModel.add(nextLink);

        String moviesJson = mapper.writeValueAsString(pagedModel);
        compareWithFile(moviesJson, "moviesPagedModel.json");
    }

    @Test
    void shouldNotSerializeMovieWithoutId() throws Exception {
        Assertions.assertThrows(JsonMappingException.class, () -> {
            Movie movie = new Movie(null, "Star Wars");
            EntityModel<Movie> entityModel = EntityModel.of(movie).add(Links.of(Link.of("http://localhost/movies/1").withSelfRel()));
            mapper.writeValueAsString(entityModel);
        });
    }

    @Test
    void shouldDeserializeSingleMovieEntityModel() throws Exception {
        JavaType movieEntityModelType = mapper.getTypeFactory().constructParametricType(EntityModel.class, Movie.class);
        File file = new ClassPathResource("movieEntityModel.json", getClass()).getFile();
        EntityModel<Movie> movieEntityModel = mapper.readValue(file, movieEntityModelType);

        Movie movie = movieEntityModel.getContent();
        assertThat(movie.getId()).isEqualTo("1");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");

        Links links = movieEntityModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/1");
    }

    @Test
    void shouldDeserializeSingleMovieRepresentationModel() throws Exception {
        JavaType movieRepresentationModelType =
                mapper.getTypeFactory().constructParametricType(RepresentationModel.class, MovieRepresentationModel.class);
        File file = new ClassPathResource("movieRepresentationModel.json", getClass()).getFile();
        MovieRepresentationModel movieRepresentationModel = mapper.readValue(file, movieRepresentationModelType);

        assertThat(movieRepresentationModel.getId()).isEqualTo("1");
        assertThat(movieRepresentationModel.getName()).isEqualTo("Star Wars");
        assertThat(movieRepresentationModel.getType()).isEqualTo("movie-type");

        Links links = movieRepresentationModel.getLinks();
        assertThat(links.hasSingleLink()).isTrue();
        assertThat(links.getLink("self").get().getHref()).isEqualTo("http://localhost/movies/7");
    }

    private void compareWithFile(String json, String fileName) throws Exception {
        File file = new ClassPathResource(fileName, getClass()).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JsonNode jsonNode = objectMapper.readValue(file, JsonNode.class);
        assertThat(json).isEqualTo(jsonNode.toString());
    }
}
