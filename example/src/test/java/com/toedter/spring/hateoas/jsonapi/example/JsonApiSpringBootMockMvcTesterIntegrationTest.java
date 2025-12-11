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

package com.toedter.spring.hateoas.jsonapi.example;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.example.director.DirectorRepository;
import com.toedter.spring.hateoas.jsonapi.example.movie.Movie;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieRepository;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Kai Toedter
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Spring Boot Integration Test with MockMvcTester")
class JsonApiSpringBootMockMvcTesterIntegrationTest {

  @Autowired
  private MockMvcTester mockMvcTester;

  @Autowired
  private WebApplicationContext context;

  @MockitoBean
  private MovieRepository movieRepository;

  @MockitoBean
  private DirectorRepository directorRepository;

  @Test
  void should_get_single_movie() {
    Movie movie = new Movie("12345", "Test Movie", 2020, 9.3, 17, null);
    movie.setId(1L);

    Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

    var result = mockMvcTester
      .get()
      .uri("/api/movies/1?include=directors")
      .accept(JSON_API);

    assertThat(result).hasStatusOk();
    assertThat(result)
      .bodyJson()
      .extractingPath("$.jsonapi")
      .asMap()
      .isNotEmpty();
    assertThat(result)
      .bodyJson()
      .extractingPath("$.jsonapi.version")
      .asString()
      .isEqualTo("1.1");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.id")
      .asString()
      .isEqualTo("1");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.type")
      .asString()
      .isEqualTo("movies");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.title")
      .asString()
      .isEqualTo("Test Movie");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.year")
      .asNumber()
      .isEqualTo(2020);
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.rating")
      .asNumber()
      .isEqualTo(9.3);
    assertThat(result)
      .bodyJson()
      .extractingPath("$.links.self")
      .asString()
      .isEqualTo("http://localhost/api/movies/1");
  }

  @Test
  void should_get_single_movie_with_include() {
    Movie movie = new Movie("12345", "Test Movie", 2020, 9.3, 17, null);
    movie.setId(1L);
    Director director = new Director(
      2L,
      "Good Director",
      Collections.singletonList(movie)
    );
    movie.addDirector(director);

    Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

    var result = mockMvcTester
      .get()
      .uri("/api/movies/1?include=directors")
      .accept(JSON_API);

    assertThat(result).hasStatusOk();
    assertThat(result)
      .bodyJson()
      .extractingPath("$.jsonapi")
      .asMap()
      .isNotEmpty();
    assertThat(result)
      .bodyJson()
      .extractingPath("$.jsonapi.version")
      .asString()
      .isEqualTo("1.1");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.id")
      .asString()
      .isEqualTo("1");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.type")
      .asString()
      .isEqualTo("movies");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.title")
      .asString()
      .isEqualTo("Test Movie");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.year")
      .asNumber()
      .isEqualTo(2020);
    assertThat(result)
      .bodyJson()
      .extractingPath("$.data.attributes.rating")
      .asNumber()
      .isEqualTo(9.3);
    assertThat(result)
      .bodyJson()
      .extractingPath("$.included")
      .asArray()
      .hasSize(1);
    assertThat(result)
      .bodyJson()
      .extractingPath("$.included[0].attributes.name")
      .asString()
      .isEqualTo("Good Director");
    assertThat(result)
      .bodyJson()
      .extractingPath("$.links.self")
      .asString()
      .isEqualTo("http://localhost/api/movies/1");
  }

  @Test
  void should_post_movie() {
    String movieJson = """
      {
        "data": {
          "type": "movies",
          "attributes": {
            "title": "Test Movie",
            "year": 2022,
            "imdbId": "imdb",
            "rating": 6.5,
            "rank": 5
          }
        }
      }""";

    Mockito.when(movieRepository.save(any())).thenAnswer(i -> {
      Movie movie = (Movie) i.getArguments()[0];
      movie.setId(42L);
      return movie;
    });

    assertThat(
      mockMvcTester
        .post()
        .uri("/api/movies")
        .contentType(JSON_API)
        .content(movieJson)
    )
      .hasStatus(201)
      .hasHeader("Location", "http://localhost/api/movies/42");
  }

  @Test
  void should_post_movie_with_jsonapi_version() {
    String movieJson = """
      {
        "jsonapi": {
          "version": "1.1"
        },
        "data": {
          "type": "movies",
          "attributes": {
            "title": "Test Movie",
            "year": 2022,
            "imdbId": "imdb",
            "rating": 6.5,
            "rank": 5
          }
        }
      }""";

    Mockito.when(movieRepository.save(any())).thenAnswer(i -> {
      Movie movie = (Movie) i.getArguments()[0];
      movie.setId(42L);
      return movie;
    });

    assertThat(
      mockMvcTester
        .post()
        .uri("/api/movies")
        .contentType(JSON_API)
        .content(movieJson)
    )
      .hasStatus(201)
      .hasHeader("Location", "http://localhost/api/movies/42");
  }
}
