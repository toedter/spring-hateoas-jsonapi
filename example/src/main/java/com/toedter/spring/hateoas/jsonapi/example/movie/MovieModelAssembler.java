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

import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import com.toedter.spring.hateoas.jsonapi.example.director.DirectorController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
class MovieModelAssembler {
    public RepresentationModel<?> toJsonApiModel(Movie movie) {
        Link selfLink = linkTo(methodOn(MovieController.class).findOne(movie.getId())).withSelfRel();

        Link moviesLink = linkTo(MovieController.class).slash("movies").withRel("movies");
        Link templatedMoviesLink = Link.of(moviesLink.getHref() + "{?page[number],page[size]}").withRel("movies");

        String relationshipSelfLink = selfLink.getHref() + "/relationships/1";

        JsonApiModelBuilder builder = jsonApiModel()
                .model(movie)
                .link(selfLink)
                .link(templatedMoviesLink);
        int i = 0;
        for (Director director : movie.getDirectors()) {
            EntityModel<Director> directorEntityModel = EntityModel.of(director);
            String relationshipRelatedLink = selfLink.getHref() + "/directors/" + i++;
            builder = builder.relationship("directors", directorEntityModel,
                    relationshipSelfLink, relationshipRelatedLink);
        }

        return builder.build();
    }

}
