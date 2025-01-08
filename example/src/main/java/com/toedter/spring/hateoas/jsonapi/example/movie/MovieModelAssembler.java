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

package com.toedter.spring.hateoas.jsonapi.example.movie;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.DIRECTORS;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.MOVIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.*;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class MovieModelAssembler {

  public RepresentationModel<?> toJsonApiModel(
    Movie movie,
    String[] fieldsMovies
  ) {
    Link selfLink = linkTo(
      methodOn(MovieController.class).findOne(movie.getId(), null, null)
    ).withSelfRel();
    String href = selfLink.getHref();
    selfLink = selfLink.withHref(href.substring(0, href.indexOf("{")));

    // Spring HATEOAS does not recognize templated links with square brackets
    // Link templatedMoviesLink = Link.of(moviesLink.getHref() + "{?page[number],page[size]}").withRel("movies");

    String relationshipSelfLink =
      selfLink.getHref() + "/relationships/" + DIRECTORS;
    String relationshipRelatedLink = selfLink.getHref() + "/" + DIRECTORS;

    final Affordance updatePartiallyAffordance = afford(
      methodOn(MovieController.class).updateMoviePartially(
        EntityModel.of(movie),
        movie.getId()
      )
    );

    final Affordance deleteAffordance = afford(
      methodOn(MovieController.class).deleteMovie(movie.getId())
    );

    JsonApiModelBuilder builder = jsonApiModel()
      .model(movie)
      .link(
        selfLink
          .andAffordance(updatePartiallyAffordance)
          .andAffordance(deleteAffordance)
      );

    if (fieldsMovies != null) {
      builder = builder.fields(MOVIES, fieldsMovies);
    }

    if (
      fieldsMovies == null || Arrays.asList(fieldsMovies).contains(DIRECTORS)
    ) {
      builder = builder
        .relationship(DIRECTORS, movie.getDirectors())
        .relationship(
          DIRECTORS,
          relationshipSelfLink,
          relationshipRelatedLink,
          null
        );
    }

    return builder.build();
  }

  public RepresentationModel<?> directorsToJsonApiModel(Movie movie) {
    Link selfLink = linkTo(
      methodOn(MovieController.class).findDirectors(movie.getId())
    ).withSelfRel();

    JsonApiModelBuilder builder = jsonApiModel()
      .model(CollectionModel.of(movie.getDirectors()))
      .relationshipWithDataArray(MOVIES)
      .link(selfLink);

    return builder.build();
  }

  public RepresentationModel<?> directorsRelationshipToJsonApiModel(
    Movie movie
  ) {
    Link selfLink = linkTo(
      methodOn(MovieController.class).findDirectorsRelationship(movie.getId())
    ).withSelfRel();
    Link relatedLink = linkTo(
      methodOn(MovieController.class).findDirectors(movie.getId())
    ).withRel("related");

    JsonApiModelBuilder builder = jsonApiModel()
      .model(CollectionModel.of(movie.getDirectors()))
      .relationshipWithDataArray(MOVIES)
      .link(selfLink)
      .link(relatedLink);

    return builder.build();
  }
}
