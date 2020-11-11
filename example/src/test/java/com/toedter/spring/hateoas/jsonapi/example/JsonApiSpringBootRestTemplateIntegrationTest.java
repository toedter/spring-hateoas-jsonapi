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

package com.toedter.spring.hateoas.jsonapi.example;

import com.toedter.spring.hateoas.jsonapi.MediaTypes;
import com.toedter.spring.hateoas.jsonapi.example.director.DirectorRepository;
import com.toedter.spring.hateoas.jsonapi.example.movie.Movie;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kai Toedter
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("Spring Boot Integration Test with RestTemplate")
public class JsonApiSpringBootRestTemplateIntegrationTest {

    @LocalServerPort
    private int randomPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private DirectorRepository directorRepository;

    @BeforeEach
    void beforeEach() {
        this.directorRepository.deleteAll();
        this.movieRepository.deleteAll();
    }

    @Test
    void should_get_single_movie() {
        Movie movie = new Movie("12345", "Test Movie", 2020, 9.3, 17, null);
        final Movie savedMovie = movieRepository.save(movie);

        final HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", MediaTypes.JSON_API_VALUE);

        final HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange("/api/movies/" + savedMovie.getId() + "?fields[movies]=title,year,rating,directors", HttpMethod.GET, entity, String.class);

        String expectedResult =
                "{\"jsonapi\":{\"version\":\"1.0\"},\"data\":{\"id\":\""
                        + savedMovie.getId()
                        + "\",\"type\":\"movies\",\"attributes\":{\"title\":\"Test Movie\",\"year\":2020,\"rating\":9.3}"
                        + ",\"relationships\":{\"directors\":{\"data\":[],\"links\":{\"self\":\"http://localhost:"
                        + this.randomPort
                        + "/api/movies/427/relationships/directors\",\"related\":\"http://localhost:"
                        + this.randomPort
                        + "/api/movies/427/directors\"}}}}"
                        + ",\"links\":{\"self\":\"http://localhost:"
                        + this.randomPort
                        + "/api/movies/427\"}}";

        assertThat(response.getBody()).isEqualTo(expectedResult);
    }
}
