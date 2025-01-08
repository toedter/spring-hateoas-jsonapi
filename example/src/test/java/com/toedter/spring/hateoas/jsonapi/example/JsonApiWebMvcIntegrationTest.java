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

package com.toedter.spring.hateoas.jsonapi.example;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.empty;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.toedter.spring.hateoas.jsonapi.example.director.DirectorRepository;
import com.toedter.spring.hateoas.jsonapi.example.movie.Movie;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieController;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author Kai Toedter
 */
@WebMvcTest(MovieController.class)
@ComponentScan("com.toedter.spring.hateoas.jsonapi")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("WebMvc Integration Test")
class JsonApiWebMvcIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private MovieRepository movieRepository;

  @MockBean
  private DirectorRepository directorRepository;

  @Test
  void should_get_single_movie() throws Exception {
    Movie movie = new Movie("12345", "Test Movie", 2020, 9.3, 17, null);
    movie.setId(1L);

    Mockito.when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

    this.mockMvc.perform(get("/api/movies/1").accept(JSON_API))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.jsonapi", is(not(empty()))))
      .andExpect(jsonPath("$.jsonapi.version", is("1.1")))
      .andExpect(jsonPath("$.data.id", is("1")))
      .andExpect(jsonPath("$.data.type", is("movies")))
      .andExpect(jsonPath("$.data.attributes.title", is("Test Movie")))
      .andExpect(jsonPath("$.data.attributes.year", is(2020)))
      .andExpect(jsonPath("$.data.attributes.rating", is(9.3)))
      .andExpect(jsonPath("$.links.self", is("http://localhost/api/movies/1")));
  }

  @Test
  void should_post_movie() throws Exception {
    String movieJson =
      "{\n" +
      "\t\"data\": {\n" +
      "\t\t\"type\": \"movies\",\n" +
      "\t\t\"attributes\": {\n" +
      "\t\t\t\"title\": \"Test Movie\",\n" +
      "\t\t\t\"year\": 2022,\n" +
      "\t\t\t\"imdbId\": \"imdb\",\n" +
      "\t\t\t\"rating\": 6.5,\n" +
      "\t\t\t\"rank\": 5\n" +
      "\t\t}\n" +
      "\t}\n" +
      "}";

    Mockito.when(movieRepository.save(any())).thenAnswer(i -> {
      Movie movie = (Movie) i.getArguments()[0];
      movie.setId(42L);
      return movie;
    });

    this.mockMvc.perform(
        post("/api/movies").contentType(JSON_API).content(movieJson)
      )
      .andExpect(status().isCreated())
      .andExpect(
        header().stringValues("location", "http://localhost/api/movies/42")
      )
      .andReturn();
  }
}
