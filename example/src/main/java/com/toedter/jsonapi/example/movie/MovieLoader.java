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

package com.toedter.jsonapi.example.movie;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.jsonapi.example.director.Director;
import com.toedter.jsonapi.example.director.DirectorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
class MovieLoader {

    @Bean
    CommandLineRunner init(MovieRepository movieRepository, DirectorRepository directorRepository) {

        return args -> {
            String moviesJson;
            ObjectMapper mapper = new ObjectMapper();

            File file = ResourceUtils.getFile("classpath:static/movie-data/movies-250.json");

            moviesJson = readFile(file.getPath(), StandardCharsets.UTF_8);
            JsonNode rootNode = mapper.readValue(moviesJson, JsonNode.class);

            JsonNode movies = rootNode.get("movies");
            int rating = 1;
            Iterator<JsonNode> iterator = movies.iterator();
            while (iterator.hasNext()) {
                JsonNode movieNode = iterator.next();
                Movie movie = createMovie(rating++, movieNode);
                movieRepository.save(movie);

                String directors = movieNode.get("Director").asText();
                List<String> directorList = Arrays.asList(directors.split(","));

                for(String directorName: directorList) {
                    Director director = directorRepository.findByName(directorName.trim());
                    if(director == null) {
                        director = new Director(directorName.trim());
                    }
                    log.info("adding movie: " + movie.getTitle() + " to " + directorName.trim());
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
        Movie movie = new Movie(imdbId, title, year, imdbRating, rank, movieImage);

        // log.info("found movie: " + rank + ": " + title + " (" + year + ") " + imdbRating);
        return movie;
    }

    private String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
