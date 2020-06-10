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

import com.toedter.spring.hateoas.jsonapi.example.RootController;
import com.toedter.spring.hateoas.jsonapi.example.movie.Movie;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieController;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(RootController.API_BASE_PATH)
public class DirectorController {

    private final DirectorRepository repository;
    private final DirectorModelAssembler directorModelAssembler;
    
    DirectorController(DirectorRepository repository, DirectorModelAssembler directorModelAssembler) {
        this.repository = repository;
        this.directorModelAssembler = directorModelAssembler;
    }

    @GetMapping("/directors")
    ResponseEntity<RepresentationModel<?>> findAll(
            @RequestParam(value = "page[number]", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "page[size]", defaultValue = "10", required = false) int pageSize) {

        final int size = pageSize;
        final int page = pageNumber;
        final PageRequest pageRequest = PageRequest.of(page, size);

        final Page<Director> pagedResult = repository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> movieResources = StreamSupport.stream(pagedResult.spliterator(), false)
                .map(directorModelAssembler::toJsonApiModel)
                .collect(Collectors.toList());

        Link selfLink = linkTo(MovieController.class).slash("movies").withSelfRel();
        Link templatedLink = Link.of(selfLink.getHref() + "?page[number]=" + page +",page[size]=" + size).withSelfRel();

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(pagedResult.getSize(), pagedResult.getNumber(), pagedResult.getTotalElements(), pagedResult.getTotalPages());
        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(movieResources, pageMetadata, templatedLink);

        final Pageable prev = pageRequest.previous();
        if (prev.getPageNumber() < page) {
            Link prevLink = Link.of(selfLink.getHref() + "?page[number]=" + prev.getPageNumber() + "&page[size]=" + prev.getPageSize()).withRel(IanaLinkRelations.PREV);
            pagedModel.add(prevLink);
        }

        final Pageable next = pageRequest.next();
        if (next.getPageNumber() > page && next.getPageNumber() < pagedResult.getTotalPages()) {
            Link nextLink = Link.of(selfLink.getHref() + "?page[number]=" + next.getPageNumber() + "&page[size]=" + next.getPageSize()).withRel(IanaLinkRelations.NEXT);
            pagedModel.add(nextLink);
        }

        if (page > 0) {
            Link firstLink = Link.of(selfLink.getHref() + "?page[number]=0&page[size]=" + size).withRel(IanaLinkRelations.FIRST);
            pagedModel.add(firstLink);
        }

        if (page < pagedResult.getTotalPages() - 1) {
            Link lastLink = Link.of(selfLink.getHref() + "?page[number]=" + (pagedResult.getTotalPages() - 1) + "&page[size]=" + size).withRel(IanaLinkRelations.LAST);
            pagedModel.add(lastLink);
        }

        final JsonApiModelBuilder jsonApiModelBuilder = jsonApiModel().entity(pagedModel);
        HashMap<Long, Movie> directors = new HashMap<>();
        for (Director director : pagedResult.getContent()) {
            for (Movie movie : director.getMovies()) {
                directors.put(movie.getId(), movie);
            }
        }

        directors.values().stream().forEach(entry -> jsonApiModelBuilder.included(EntityModel.of(entry)));

        final RepresentationModel<?> pagedJsonApiModel = jsonApiModelBuilder.build();
        return ResponseEntity.ok(pagedJsonApiModel);

    }

    @GetMapping("/directors/{id}")
    public ResponseEntity<? extends RepresentationModel<?>> findOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(directorModelAssembler::toJsonApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
