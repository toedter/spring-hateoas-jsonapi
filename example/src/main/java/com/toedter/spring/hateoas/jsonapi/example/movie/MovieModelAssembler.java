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

package com.toedter.spring.hateoas.jsonapi.example.movie;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
class MovieModelAssembler {

    private static final String DIRECTORS = "directors";

    public RepresentationModel<?> toJsonApiModel(Movie movie, String[] fieldsMovies) {
        Link selfLink = linkTo(methodOn(MovieController.class).findOne(movie.getId(), null)).withSelfRel();
        String href = selfLink.getHref();
        selfLink = selfLink.withHref(href.substring(0,href.indexOf("{")));

        // TODO: Spring HATEOAS does not recognize templated links with square brackets
        // Link templatedMoviesLink = Link.of(moviesLink.getHref() + "{?page[number],page[size]}").withRel("movies");

        String relationshipSelfLink = selfLink.getHref() + "/relationships/" + DIRECTORS;
        String relationshipRelatedLink = selfLink.getHref() + "/" + DIRECTORS;

        JsonApiModelBuilder builder = jsonApiModel()
                .model(movie)
                .link(selfLink);

        if (fieldsMovies != null) {
            builder = builder.fields("movies", fieldsMovies);
        }

        if (fieldsMovies == null || Arrays.asList(fieldsMovies).contains("directors")) {
            builder = builder
                    .relationship(DIRECTORS, movie.getDirectors())
                    .relationship(DIRECTORS, relationshipSelfLink, relationshipRelatedLink, null);
        }

        return builder.build();
    }

    public RepresentationModel<?> directorsToJsonApiModel(Movie movie) {
        Link selfLink = linkTo(methodOn(MovieController.class).findDirectors(movie.getId())).withSelfRel();

        JsonApiModelBuilder builder = jsonApiModel()
                .model(CollectionModel.of(movie.getDirectors()))
                .relationshipWithDataArray("movies")
                .link(selfLink);

        return builder.build();
    }
}
