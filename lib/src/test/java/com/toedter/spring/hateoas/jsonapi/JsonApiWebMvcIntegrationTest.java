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

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.toedter.spring.hateoas.jsonapi.support.MovieDerivedWithTypeForClass;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithAnnotations;
import com.toedter.spring.hateoas.jsonapi.support.WebMvcMovieController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author Kai Toedter
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApi Web MVC Integration Test")
class JsonApiWebMvcIntegrationTest extends JsonApiTestBase {

  @Autowired
  WebApplicationContext context;

  MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    this.mockMvc = webAppContextSetup(this.context).build();
    WebMvcMovieController.reset();
  }

  @Test
  void should_get_single_movie() throws Exception {
    String movieJson =
      this.mockMvc.perform(get("/movies/1").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "movieEntityModelWithLinks.json");
  }

  @Test
  void should_get_collection_of_movies() throws Exception {
    String moviesJson =
      this.mockMvc.perform(get("/movies").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(moviesJson, "moviesCollectionModel.json");
  }

  @Test
  void should_get_last_seen_movie() throws Exception {
    String movieJson =
      this.mockMvc.perform(get("/movieWithLastSeen").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "movieWithLastSeen.json");
  }

  @Test
  void should_create_new_movie() throws Exception {
    String input = readFile("postMovie.json");

    this.mockMvc.perform(post("/movies").content(input).contentType(JSON_API))
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3")
      );

    String movieJson =
      this.mockMvc.perform(get("/movies/3").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "movieCreated.json");
  }

  @Test
  void should_create_new_movie_with_rating() throws Exception {
    String input = readFile("postMovieWithRating.json");

    this.mockMvc.perform(
        post("/moviesWithPolymorphism").content(input).contentType(JSON_API)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3")
      );

    String movieJson =
      this.mockMvc.perform(get("/moviesWithDirectors/3").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "polymorphicMovie.json");
  }

  @Test
  void should_create_new_polymorphic_movie_with_custom_type() throws Exception {
    String input = readFile("postMovieWithCustomType.json");

    this.mockMvc.perform(
        post("/moviesWithJsonApiTypePolymorphism")
          .content(input)
          .contentType(JSON_API)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3")
      );

    String movieJson =
      this.mockMvc.perform(get("/movies/3").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "polymorphicMovieWithCustomType.json");
  }

  @Test
  void should_not_create_new_polymorphic_movie_with_custom_type()
    throws Exception {
    String input = readFile("postMovieWithCustomType2.json");

    Assertions.assertThrows(Exception.class, () ->
      this.mockMvc.perform(
          post("/moviesWithJsonApiTypePolymorphism")
            .content(input)
            .contentType(JSON_API)
        )
    );
  }

  @Test
  void should_create_new_movie_with_relationships() throws Exception {
    String input = readFile("postMovieWithTwoRelationships.json");

    this.mockMvc.perform(
        post("/moviesWithDirectors").content(input).contentType(JSON_API)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3")
      );

    String movieJson =
      this.mockMvc.perform(get("/moviesWithDirectors/3").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "movieCreatedWithDirectors.json");
  }

  @Test
  void should_create_new_movie_with_single_relationship() throws Exception {
    String input = readFile("postMovieWithOneRelationship.json");

    this.mockMvc.perform(
        post("/moviesWithSingleDirector").content(input).contentType(JSON_API)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3")
      );

    String movieJson =
      this.mockMvc.perform(get("/moviesWithDirectors/3").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "movieCreatedWithSingleDirector.json");
  }

  @Test
  void should_create_instant() throws Exception {
    String input = readFile("movieWithLastSeen.json");

    this.mockMvc.perform(
        post("/movieWithLastSeen").content(input).contentType(JSON_API)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header()
          .stringValues(
            HttpHeaders.LOCATION,
            "http://localhost/movieWithLastSeen"
          )
      );
  }

  @Test
  void should_patch_movie() throws Exception {
    String input = readFile("patchMovie.json");

    this.mockMvc.perform(
        patch("/movies/1").content(input).contentType(JSON_API)
      )
      .andExpect(status().isNoContent())
      .andExpect(
        header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/1")
      );

    String movieJson =
      this.mockMvc.perform(get("/movies/1").accept(JSON_API))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(movieJson, "patchedMovie.json");
  }

  @Test
  void should_return_error() throws Exception {
    String errorJson =
      this.mockMvc.perform(get("/error").accept(JSON_API))
        .andExpect(status().isBadRequest())
        .andReturn()
        .getResponse()
        .getContentAsString();

    compareWithFile(errorJson, "errorsMvcExample.json");
  }

  @Configuration
  @WebAppConfiguration
  @EnableWebMvc
  @EnableHypermediaSupport(type = {})
  static class TestConfig {

    @Bean
    WebMvcMovieController movieController() {
      return new WebMvcMovieController();
    }

    @Bean
    JsonApiMediaTypeConfiguration jsonApiMediaTypeConfiguration(
      ObjectProvider<JsonApiConfiguration> configuration,
      AutowireCapableBeanFactory beanFactory
    ) {
      return new JsonApiMediaTypeConfiguration(configuration, beanFactory);
    }

    @Bean
    JsonApiConfiguration jsonApiConfiguration() {
      return new JsonApiConfiguration()
        .withObjectMapperCustomizer(objectMapper -> {
          objectMapper.registerModule(new JavaTimeModule());
          objectMapper.configure(
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            false
          );
        })
        .withTypeForClass(MovieDerivedWithTypeForClass.class, "my-movies")
        .withTypeForClass(MovieWithAnnotations.class, "my-movies-2")
        .withTypeForClassUsedForDeserialization(true);
    }
  }
}
