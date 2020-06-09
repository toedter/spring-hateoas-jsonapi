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

package com.toedter.jsonapi.example.movie;

import com.toedter.jsonapi.example.RootController;
import com.toedter.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(RootController.API_BASE_PATH)
public class MovieController {

    private final MovieRepository repository;
    private final MovieModelAssembler movieModelAssembler;

    MovieController(MovieRepository repository, MovieModelAssembler movieModelAssembler) {
        this.repository = repository;
        this.movieModelAssembler = movieModelAssembler;
    }

    @GetMapping("/movies")
    ResponseEntity<RepresentationModel<?>> findAll(
            @RequestParam(value = "page[number]", defaultValue = "0", required = false) int pageNumber,
            @RequestParam(value = "page[size]", defaultValue = "10", required = false) int pageSize) {

        final int size = pageSize;
        final int page = pageNumber;
        final PageRequest pageRequest = PageRequest.of(page, size);

        final Page<Movie> pagedResult = repository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> movieResources =
                StreamSupport.stream(pagedResult.spliterator(), false)
                .map(movieModelAssembler::toJsonApiModel)
                .collect(Collectors.toList());

        Link selfLink = linkTo(MovieController.class).slash("movies").withSelfRel();
        Link templatedLink = Link.of(selfLink.getHref() + "?page[number]=" + page + ",page[size]=" + size).withSelfRel();

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(
                        pagedResult.getSize(),
                        pagedResult.getNumber(),
                        pagedResult.getTotalElements(),
                        pagedResult.getTotalPages());

        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(movieResources, pageMetadata, templatedLink);

        final Pageable prev = pageRequest.previous();
        if (prev.getPageNumber() < page) {
            Link prevLink = Link.of(selfLink.getHref() + "?page[number]=" + prev.getPageNumber() + "&page[size]="
                    + prev.getPageSize()).withRel(IanaLinkRelations.PREV);
            pagedModel.add(prevLink);
        }

        final Pageable next = pageRequest.next();
        if (next.getPageNumber() > page && next.getPageNumber() < pagedResult.getTotalPages()) {
            Link nextLink = Link.of(selfLink.getHref() + "?page[number]=" + next.getPageNumber() + "&page[size]="
                    + next.getPageSize()).withRel(IanaLinkRelations.NEXT);
            pagedModel.add(nextLink);
        }

        if (page > 0) {
            Link firstLink = Link.of(selfLink.getHref() + "?page[number]=0&page[size]=" + size)
                    .withRel(IanaLinkRelations.FIRST);
            pagedModel.add(firstLink);
        }

        if (page < pagedResult.getTotalPages() - 1) {
            Link lastLink = Link.of(selfLink.getHref() + "?page[number]=" + (pagedResult.getTotalPages() - 1)
                    + "&page[size]=" + size).withRel(IanaLinkRelations.LAST);
            pagedModel.add(lastLink);
        }

        final JsonApiModelBuilder jsonApiModelBuilder = jsonApiModel().entity(pagedModel);
        HashMap<Long, Director> directors = new HashMap<>();
        for (Movie movie : pagedResult.getContent()) {
            for (Director director : movie.getDirectors()) {
                directors.put(director.getId(), director);
            }
        }

        directors.values().stream().forEach(entry -> jsonApiModelBuilder.included(EntityModel.of(entry)));

        final RepresentationModel<?> pagedJsonApiModel = jsonApiModelBuilder.link(templatedLink).build();

        return ResponseEntity.ok(pagedJsonApiModel);
    }

    @PostMapping("/movies")
    ResponseEntity<?> newMovie(@RequestBody Movie movie) {
        Movie savedMovie = repository.save(movie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(movie);

        return movieRepresentationModel
                .getLink(IanaLinkRelations.SELF)
                .map(Link::getHref)
                .map(href -> {
                    try {
                        return new URI(href);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(uri -> ResponseEntity.noContent().location(uri).build())
                .orElse(ResponseEntity.badRequest().body("Unable to create " + movie));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<? extends RepresentationModel<?>> findOne(@PathVariable Long id) {

        return repository.findById(id)
                .map(movieModelAssembler::toJsonApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/movies/{id}")
    ResponseEntity<?> updateMovie(@RequestBody Movie movie, @PathVariable Long id) {

        movie.setId(id);

        final Movie savedMovie = repository.save(movie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(savedMovie);

        return movieRepresentationModel
                .getLink(IanaLinkRelations.SELF)
                .map(Link::getHref).map(href -> {
                    try {
                        return new URI(href);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .map(uri -> ResponseEntity.noContent().location(uri).build()) //
                .orElse(ResponseEntity.badRequest().body("Unable to update " + movie));
    }

    @PatchMapping("/movies/{id}")
    ResponseEntity<?> updateMoviePartially(@RequestBody Movie movie, @PathVariable Long id) {

        Movie existingMovie = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id.toString()));
        existingMovie.update(movie);

        repository.save(existingMovie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(movie);

        return movieRepresentationModel
                .getLink(IanaLinkRelations.SELF)
                .map(Link::getHref).map(href -> {
                    try {
                        return new URI(href);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }) //
                .map(uri -> ResponseEntity.noContent().location(uri).build()) //
                .orElse(ResponseEntity.badRequest().body("Unable to update " + existingMovie + " partially"));
    }

    @DeleteMapping("/movies/{id}")
    ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        Optional<Movie> optional = repository.findById(id);
        if (optional.isPresent()) {
            Movie movie = optional.get();
            for (Director director : movie.getDirectors()) {
                director.deleteMovie(movie);
            }
            repository.deleteById(id);
        }

        return ResponseEntity.noContent().build();
    }
}
