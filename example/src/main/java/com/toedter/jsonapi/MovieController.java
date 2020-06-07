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

package com.toedter.jsonapi;

import com.toedter.spring.hateoas.jsonapi.JsonApiResourceModelBuilder;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.toedter.spring.hateoas.jsonapi.JsonApiResourceModelBuilder.jsonApiModel;

@RestController
public class MovieController {
    @GetMapping("/movies")
    ResponseEntity<CollectionModel<RepresentationModel<?>>> findAll() {
        Movie movie1 = new Movie("1", "Star Wars");
        Director director1 = new Director("1", "George Lucas");
        RepresentationModel<?> movie1Model = jsonApiModel()
                .entity(movie1)
                .link(Link.of("http://localhost:8080/movies/1").withSelfRel())
                .relationship("directors", EntityModel.of(director1))
                .build();

        Movie movie2 = new Movie("2", "The Matrix");
        Director director2 = new Director("2", "Lana Wachowski");
        Director director3 = new Director("3", "Lilly Wachowski");

        RepresentationModel<?> movie2Model = jsonApiModel()
                .entity(movie2)
                .link(Link.of("http://localhost:8080/movies/2").withSelfRel())
                .relationship("directors", EntityModel.of(director2))
                .relationship("directors", EntityModel.of(director3))
                .build();

        List<RepresentationModel<?>> movies = new ArrayList<>();
        movies.add(movie1Model);
        movies.add(movie2Model);

        CollectionModel<RepresentationModel<?>> collectionModel =
                CollectionModel.of(movies).add(Links.of(Link.of("http://localhost/movies").withSelfRel()));

        return ResponseEntity.ok(collectionModel);
    }
}

