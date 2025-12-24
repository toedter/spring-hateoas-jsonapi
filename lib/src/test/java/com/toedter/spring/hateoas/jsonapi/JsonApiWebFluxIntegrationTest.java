/*
 * Copyright 2025 the original author or authors.
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

import com.toedter.spring.hateoas.jsonapi.support.WebFluxMovieController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebTestClientConfigurer;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.config.EnableWebFlux;

/**
 * @author Kai Toedter
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApi Web Flux Integration Test")
class JsonApiWebFluxIntegrationTest extends JsonApiTestBase {

  @Autowired
  WebTestClient testClient;

  @BeforeEach
  void setUp() {
    WebFluxMovieController.reset();
  }

  @Test
  void should_get_single_movie() throws Exception {
    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/movies/1")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "movieEntityModelWithLinks.json");
  }

  @Test
  void should_get_collection_of_movies() throws Exception {
    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/movies")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "moviesCollectionModel.json");
  }

  @Test
  void should_create_new_movie() throws Exception {
    String input = readFile("postMovie.json");

    this.testClient.post()
      .uri("http://localhost/movies")
      .contentType(JSON_API)
      .bodyValue(input)
      .exchange()
      .expectStatus()
      .isCreated()
      .expectHeader()
      .valueEquals(HttpHeaders.LOCATION, "http://localhost/movies/3");

    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/movies/3")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "movieCreated.json");
  }

  @Test
  void should_create_new_movie_with_relationships() throws Exception {
    String input = readFile("postMovieWithTwoRelationships.json");

    this.testClient.post()
      .uri("http://localhost/moviesWithDirectors")
      .contentType(JSON_API)
      .bodyValue(input)
      .exchange()
      .expectStatus()
      .isCreated()
      .expectHeader()
      .valueEquals(HttpHeaders.LOCATION, "http://localhost/movies/3");

    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/moviesWithDirectors/3")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "movieCreatedWithDirectors.json");
  }

  @Test
  void should_patch_movie() throws Exception {
    String input = readFile("patchMovie.json");

    this.testClient.patch()
      .uri("http://localhost/movies/1")
      .contentType(JSON_API)
      .bodyValue(input)
      .exchange()
      .expectStatus()
      .isNoContent()
      .expectHeader()
      .valueEquals(HttpHeaders.LOCATION, "http://localhost/movies/1");

    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/movies/1")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isOk()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "patchedMovie.json");
  }

  @Test
  void should_return_error() throws Exception {
    EntityExchangeResult<String> result = this.testClient.get()
      .uri("http://localhost/error")
      .accept(JSON_API)
      .exchange()
      .expectStatus()
      .isBadRequest()
      .expectHeader()
      .contentType(JSON_API)
      .expectBody(String.class)
      .returnResult();

    compareWithFile(result.getResponseBody(), "errorsMvcExample.json");
  }

  @Configuration
  @WebAppConfiguration
  @EnableWebFlux
  @EnableHypermediaSupport(type = {})
  static class TestConfig {

    @Bean
    WebFluxMovieController movieController() {
      return new WebFluxMovieController();
    }

    @Bean
    JsonApiMediaTypeConfiguration jsonApiMediaTypeConfiguration(
      ObjectProvider<JsonApiConfiguration> configuration,
      AutowireCapableBeanFactory beanFactory
    ) {
      return new JsonApiMediaTypeConfiguration(configuration, beanFactory);
    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Bean
    WebTestClient webTestClient(
      HypermediaWebTestClientConfigurer configurer,
      ApplicationContext ctx
    ) {
      return WebTestClient.bindToApplicationContext(ctx)
        .build()
        .mutateWith(configurer);
    }
  }
}
