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

package com.toedter.spring.hateoas.jsonapi.support;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.reactive.WebFluxLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kai Toedter
 */
@RestController
public class WebFluxMovieController {

  private static Map<Integer, Movie> movies;

  public static void reset() {
    movies = new TreeMap<>();

    movies.put(1, new Movie("1", "Star Wars"));
    movies.put(2, new Movie("2", "Avengers"));
  }

  @GetMapping("/movies")
  public Mono<CollectionModel<EntityModel<Movie>>> all() {
    WebFluxMovieController controller = methodOn(WebFluxMovieController.class);

    return Flux.fromIterable(movies.keySet())
      .flatMap(this::findOne)
      .collectList()
      .flatMap(resources ->
        WebFluxLinkBuilder.linkTo(controller.all())
          .withSelfRel()
          .toMono()
          .map(selfLink -> CollectionModel.of(resources, selfLink))
      );
  }

  @GetMapping("/movies/{id}")
  public Mono<EntityModel<Movie>> findOne(@PathVariable Integer id) {
    WebFluxMovieController controller = methodOn(WebFluxMovieController.class);

    Mono<Link> selfLink = WebFluxLinkBuilder.linkTo(controller.findOne(id))
      .withSelfRel()
      .toMono();

    Movie movie = movies.get(id);
    return selfLink.map(links -> EntityModel.of(movie, links));
  }

  @GetMapping("/moviesWithDirectors/{id}")
  public Mono<RepresentationModel<?>> findOneWidthDirectors(
    @PathVariable Integer id
  ) {
    Movie movie = movies.get(id);
    List<Director> directors = ((MovieWithDirectors) movie).getDirectors();
    JsonApiModelBuilder model = JsonApiModelBuilder.jsonApiModel().model(movie);
    for (Director director : directors) {
      model = model.relationship("directors", director);
    }
    RepresentationModel<?> jsonApiModel = model.build();
    return Mono.just(jsonApiModel);
  }

  @GetMapping("/movieWithClassType")
  public Mono<RepresentationModel<?>> movieWithClassType() {
    Movie movie = new Movie("1", "Star Wars");
    return Mono.just(new MovieRepresentationModelWithoutJsonApiType(movie));
  }

  @PostMapping("/movies")
  public Mono<ResponseEntity<?>> newMovie(
    @RequestBody Mono<EntityModel<Movie>> movie
  ) {
    return movie
      .flatMap(resource -> {
        int newMovieId = movies.size() + 1;
        assert resource.getContent() != null;
        resource.getContent().setId("" + newMovieId);
        movies.put(newMovieId, resource.getContent());
        return findOne(newMovieId);
      })
      .map(findOne ->
        ResponseEntity.created(
          findOne.getRequiredLink(IanaLinkRelations.SELF).toUri()
        ).build()
      );
  }

  @PostMapping("/moviesWithDirectors")
  public Mono<ResponseEntity<?>> newMovieWithDirectors(
    @RequestBody Mono<EntityModel<MovieWithDirectors>> movie
  ) {
    return movie
      .flatMap(resource -> {
        int newMovieId = movies.size() + 1;
        assert resource.getContent() != null;
        resource.getContent().setId("" + newMovieId);
        movies.put(newMovieId, resource.getContent());
        return findOne(newMovieId);
      })
      .map(findOne ->
        ResponseEntity.created(
          findOne.getRequiredLink(IanaLinkRelations.SELF).toUri()
        ).build()
      );
  }

  @PatchMapping("/movies/{id}")
  public Mono<ResponseEntity<?>> partiallyUpdateMovie(
    @RequestBody Mono<EntityModel<Movie>> movie,
    @PathVariable Integer id
  ) {
    return movie
      .flatMap(resource -> {
        Movie newMovie = movies.get(id);
        assert resource.getContent() != null;
        if (resource.getContent().getTitle() != null) {
          newMovie = newMovie.withTitle(resource.getContent().getTitle());
        }

        movies.put(id, newMovie);

        return findOne(id);
      })
      .map(findOne ->
        ResponseEntity.noContent()
          .location(findOne.getRequiredLink(IanaLinkRelations.SELF).toUri())
          .build()
      );
  }

  @GetMapping("/error")
  public ResponseEntity<?> error() {
    JsonApiErrors body = JsonApiErrors.create()
      .withError(
        JsonApiError.create()
          .withAboutLink("http://movie-db.com/problem")
          .withTitle("Movie-based problem")
          .withStatus(HttpStatus.BAD_REQUEST.toString())
          .withDetail("This is a test case")
      );
    return ResponseEntity.badRequest().body(body);
  }
}
