/*
 * Copyright 2021 the original author or authors.
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

package com.toedter.spring.hateoas.jsonapi.example.director;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
class DirectorModelAssembler {

    private static final String MOVIES = "movies";

    public RepresentationModel<?> toJsonApiModel(Director director, String[] fieldsDirectors) {
        Link selfLink = linkTo(methodOn(DirectorController.class).findOne(director.getId(), null)).withSelfRel();

        Link directorsLink = linkTo(DirectorController.class).slash("directors").withRel("directors");
        Link templatedDirectorsLink = Link.of(directorsLink.getHref() + "{?page[number],page[size]}").withRel("directors");

        JsonApiModelBuilder builder = jsonApiModel()
                .model(director)
                .link(selfLink)
                .link(templatedDirectorsLink);

        if (fieldsDirectors != null) {
            builder = builder.fields("directors", fieldsDirectors);
        }

        if (fieldsDirectors == null || Arrays.asList(fieldsDirectors).contains("movies")) {
            builder = builder.relationship(MOVIES, director.getMovies());
        }

        return builder.build();
    }
}
