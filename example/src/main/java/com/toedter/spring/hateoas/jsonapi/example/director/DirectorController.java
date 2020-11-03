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

package com.toedter.spring.hateoas.jsonapi.example.director;

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import com.toedter.spring.hateoas.jsonapi.example.RootController;
import com.toedter.spring.hateoas.jsonapi.example.movie.Movie;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class DirectorController {

    private final DirectorRepository repository;
    private final DirectorModelAssembler directorModelAssembler;

    DirectorController(DirectorRepository repository, DirectorModelAssembler directorModelAssembler) {
        this.repository = repository;
        this.directorModelAssembler = directorModelAssembler;
    }

    @GetMapping("/directors")
    public ResponseEntity<RepresentationModel<?>> findAll(
            @RequestParam(value = "page[number]", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "page[size]", defaultValue = "10", required = false) int pageSize,
            @RequestParam(value = "included", required = false) String[] included,
            @RequestParam(value = "fields[movies]", required = false) String[] fieldsMovies,
            @RequestParam(value = "fields[directors]", required = false) String[] fieldsDirectors) {

        final PageRequest pageRequest = PageRequest.of(pageNumber, pageSize);

        final Page<Director> pagedResult = repository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> movieResources = StreamSupport.stream(pagedResult.spliterator(), false)
                .map(director -> directorModelAssembler.toJsonApiModel(director, fieldsDirectors))
                .collect(Collectors.toList());

        Link selfLink = linkTo(DirectorController.class).slash(
                "directors?page[number]=" + pagedResult.getNumber()
                        + "&page[size]=" + pagedResult.getSize()).withSelfRel();

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(pagedResult.getSize(), pagedResult.getNumber(), pagedResult.getTotalElements(), pagedResult.getTotalPages());
        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(movieResources, pageMetadata, selfLink);

        String pageLinksBase = linkTo(MovieController.class).slash("directors").withSelfRel().getHref();
        JsonApiModelBuilder jsonApiModelBuilder =
                jsonApiModel().model(pagedModel).pageLinks(pageLinksBase);

        if (fieldsMovies != null) {
            jsonApiModelBuilder = jsonApiModelBuilder.fields("movies", fieldsMovies);
        }

        final JsonApiModelBuilder finalJsonApiModelBuilder = jsonApiModelBuilder;
        if (included != null && included.length == 1 && included[0].equals("movies")) {
            HashMap<Long, Movie> movies = new HashMap<>();
            for (Director director : pagedResult.getContent()) {
                for (Movie movie : director.getMovies()) {
                    movies.put(movie.getId(), movie);
                }
            }
            movies.values().forEach(entry -> finalJsonApiModelBuilder.included(EntityModel.of(entry)));
        }


        final RepresentationModel<?> pagedJsonApiModel = jsonApiModelBuilder.build();
        return ResponseEntity.ok(pagedJsonApiModel);
    }

    @GetMapping("/directors/{id}")
    public ResponseEntity<? extends RepresentationModel<?>> findOne(
            @PathVariable Long id,
            @RequestParam(value = "fields[directors]", required = false) String[] fieldsDirectors) {
        return repository.findById(id)
                .map(director -> directorModelAssembler.toJsonApiModel(director, fieldsDirectors))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
