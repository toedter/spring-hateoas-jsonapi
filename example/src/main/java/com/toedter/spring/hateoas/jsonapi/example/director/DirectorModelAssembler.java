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

package com.toedter.spring.hateoas.jsonapi.example.director;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.DIRECTORS;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.MOVIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
@Slf4j
class DirectorModelAssembler {

    public RepresentationModel<?> toJsonApiModel(Director director, String[] fieldsDirectors) {
        Link selfLink = linkTo(methodOn(DirectorController.class).findOne(director.getId(), null, null)).withSelfRel();
        String href = selfLink.getHref();
        selfLink = selfLink.withHref(href.substring(0,href.indexOf("{")));

        Link directorsLink = linkTo(DirectorController.class).slash(DIRECTORS).withRel(DIRECTORS);
        Link templatedDirectorsLink = Link.of(directorsLink.getHref() + "{?page[number],page[size]}").withRel(DIRECTORS);

        JsonApiModelBuilder builder = jsonApiModel()
                .model(director)
                .link(selfLink)
                .link(templatedDirectorsLink);

        if (fieldsDirectors != null) {
            builder = builder.fields(DIRECTORS, fieldsDirectors);
        }

        if (fieldsDirectors == null || Arrays.asList(fieldsDirectors).contains(MOVIES)) {
            builder = builder.relationship(MOVIES, director.getMovies());
        }

        return builder.build();
    }
}
