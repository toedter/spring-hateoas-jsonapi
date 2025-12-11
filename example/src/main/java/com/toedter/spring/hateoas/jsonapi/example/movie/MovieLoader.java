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

package com.toedter.spring.hateoas.jsonapi.example.movie;

import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.MOVIES;

import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.example.director.DirectorRepository;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Component
@Slf4j
class MovieLoader {

  @Bean
  CommandLineRunner init(
    MovieRepository movieRepository,
    DirectorRepository directorRepository
  ) {
    return args -> {
      String moviesJson;
      JsonMapper mapper = JsonMapper.builder().build();

      File file = ResourceUtils.getFile(
        "classpath:static/movie-data/movies-250.json"
      );

      moviesJson = readFile(file.getPath());
      JsonNode rootNode = mapper.readValue(moviesJson, JsonNode.class);

      JsonNode movies = rootNode.get(MOVIES);
      int rating = 1;
      for (JsonNode movieNode : movies) {
        Movie movie = createMovie(rating++, movieNode);
        movieRepository.save(movie);

        String directors = movieNode.get("Director").asText();
        String[] directorList = directors.split(",");

        for (String directorName : directorList) {
          Director director = directorRepository.findByName(
            directorName.trim()
          );
          if (director == null) {
            director = new Director(directorName.trim());
          }
          log.info(
            "adding movie \"" +
              movie.getTitle() +
              "\" to director \"" +
              directorName.trim() +
              "\"."
          );
          director.addMovie(movie);
          directorRepository.save(director);
          movie.addDirector(director);
          movieRepository.save(movie);
        }
      }
    };
  }

  private Movie createMovie(int rank, JsonNode rootNode) {
    String title = rootNode.get("Title").asText();
    String imdbId = rootNode.get("imdbID").asText();

    long year = rootNode.get("Year").asLong();
    double imdbRating = rootNode.get("imdbRating").asDouble();

    String movieImage = "/static/movie-data/thumbs/" + imdbId + ".jpg";

    return new Movie(imdbId, title, year, imdbRating, rank, movieImage);
  }

  private String readFile(String path) throws IOException {
    byte[] encoded = Files.readAllBytes(Paths.get(path));
    return new String(encoded, StandardCharsets.UTF_8);
  }
}
