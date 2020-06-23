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
package com.toedter.spring.hateoas.jsonapi.support;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * @author Kai Toedter
 */
@RestController
public class WebMvcMovieController {

    private static Map<Integer, Movie> MOVIES;

    public static void reset() {

        MOVIES = new TreeMap<>();

        MOVIES.put(1, new Movie("1", "Star Wars"));
        MOVIES.put(2, new Movie("2", "Avengers"));
    }

    @GetMapping("/movies")
    public CollectionModel<EntityModel<Movie>> all() {
        WebMvcMovieController controller = methodOn(WebMvcMovieController.class);

        Link selfLink = linkTo(controller.all()).withSelfRel();

        return IntStream.range(1, MOVIES.size() + 1)
                .mapToObj(this::findOne)
                .collect(Collectors.collectingAndThen(Collectors.toList(), it -> CollectionModel.of(it, selfLink)));
    }

    @GetMapping("/movies/{id}")
    public EntityModel<Movie> findOne(@PathVariable Integer id) {
        WebMvcMovieController controller = methodOn(WebMvcMovieController.class);

        Link selfLink = linkTo(controller.findOne(id)).withSelfRel();

        Movie movie = MOVIES.get(id);
        return EntityModel.of(
                movie,
                selfLink);
    }

    @PostMapping("/movies")
    public ResponseEntity<?> newMovie(@RequestBody EntityModel<Movie> movie) {
        int newMovieId = MOVIES.size() + 1;
        String newMovieIdString = "" + newMovieId;
        Movie movieContent = movie.getContent();
        assert movieContent != null;
        movieContent.setId(newMovieIdString);
        MOVIES.put(newMovieId, movieContent);

        Link link = linkTo(methodOn(getClass()).findOne(newMovieId)).withSelfRel().expand();

        return ResponseEntity.created(link.toUri()).build();
    }

    @PutMapping("/movies/{id}")
    public ResponseEntity<?> updateMovie(@RequestBody EntityModel<Movie> movie, @PathVariable Integer id) {

        MOVIES.put(id, movie.getContent());

        Link link = linkTo(methodOn(getClass()).findOne(id)).withSelfRel().expand();

        return ResponseEntity.noContent()
                .location(link.toUri())
                .build();
    }

    @PatchMapping("/movies/{id}")
    public ResponseEntity<?> partiallyUpdateMovie(@RequestBody EntityModel<Movie> movie,
                                                  @PathVariable Integer id) {

        Movie newMovie = MOVIES.get(id);

        assert movie.getContent() != null;

        if (movie.getContent().getTitle() != null) {
            newMovie = newMovie.withTitle(movie.getContent().getTitle());
        }

        MOVIES.put(id, newMovie);

        return ResponseEntity
                .noContent()
                .location(findOne(id)
                        .getRequiredLink(IanaLinkRelations.SELF)
                        .toUri())
                .build();
    }

    @GetMapping("/error")
    public ResponseEntity<?> error() {
        // tag::errors-builder[]
        return ResponseEntity.badRequest().body(
                JsonApiErrors.create().withError(
                        JsonApiError.create()
                                .withAboutLink("http://movie-db.com/problem")
                                .withTitle("Movie-based problem")
                                .withStatus(HttpStatus.BAD_REQUEST.toString())
                                .withDetail("This is a test case")));
        // end::errors-builder[]
    }
}
