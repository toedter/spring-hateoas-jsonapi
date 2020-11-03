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
import com.toedter.spring.hateoas.jsonapi.example.RootController;
import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class MovieController {

    private final MovieRepository repository;
    private final MovieModelAssembler movieModelAssembler;

    MovieController(MovieRepository repository, MovieModelAssembler movieModelAssembler) {
        this.repository = repository;
        this.movieModelAssembler = movieModelAssembler;
    }

    @GetMapping("/movies")
    public ResponseEntity<RepresentationModel<?>> findAll(
            @RequestParam(value = "page[number]", defaultValue = "0", required = false) int page,
            @RequestParam(value = "page[size]", defaultValue = "10", required = false) int size,
            @RequestParam(value = "included", required = false) String[] included,
            @RequestParam(value = "fields[movies]", required = false) String[] fieldsMovies) {

        final PageRequest pageRequest = PageRequest.of(page, size);

        final Page<Movie> pagedResult = repository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> movieResources =
                StreamSupport.stream(pagedResult.spliterator(), false)
                        .map(movie -> movieModelAssembler.toJsonApiModel(movie, fieldsMovies))
                        .collect(Collectors.toList());

        Link selfLink = linkTo(MovieController.class).slash(
                "movies?page[number]=" + pagedResult.getNumber()
                        + "&page[size]=" + pagedResult.getSize()).withSelfRel();

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(
                        pagedResult.getSize(),
                        pagedResult.getNumber(),
                        pagedResult.getTotalElements(),
                        pagedResult.getTotalPages());

        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(movieResources, pageMetadata);

        String pageLinksBase = linkTo(MovieController.class).slash("movies").withSelfRel().getHref();
        final JsonApiModelBuilder jsonApiModelBuilder =
                jsonApiModel().model(pagedModel).link(selfLink).pageLinks(pageLinksBase);

        // tag::relationship-inclusion[]
        if (included != null && included.length == 1 && included[0].equals("directors")) {
            HashMap<Long, Director> directors = new HashMap<>();
            for (Movie movie : pagedResult.getContent()) {
                for (Director director : movie.getDirectors()) {
                    directors.put(director.getId(), director);
                }
            }
            directors.values().forEach(entry -> jsonApiModelBuilder.included(EntityModel.of(entry)));
        }
        // end::relationship-inclusion[]

        final RepresentationModel<?> pagedJsonApiModel = jsonApiModelBuilder.build();

        return ResponseEntity.ok(pagedJsonApiModel);
    }

    @PostMapping("/movies")
    public ResponseEntity<?> newMovie(@RequestBody Movie movie) {
        repository.save(movie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(movie, null);

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
                .map(uri -> ResponseEntity.created(uri).build())
                .orElse(ResponseEntity.badRequest().body("Unable to create " + movie));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<? extends RepresentationModel<?>> findOne(
            @PathVariable Long id,
            @RequestParam(value = "fields[movies]", required = false) String[] filterMovies) {

        return repository.findById(id)
                .map(movie -> movieModelAssembler.toJsonApiModel(movie, filterMovies))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/movies/{id}/directors")
    public ResponseEntity<? extends RepresentationModel<?>> findDirectors(@PathVariable Long id) {

        return repository.findById(id)
                .map(movieModelAssembler::directorsToJsonApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/movies/{id}")
    public ResponseEntity<?> updateMoviePartially(@RequestBody Movie movie, @PathVariable Long id) {

        Movie existingMovie = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id.toString()));
        existingMovie.update(movie);

        repository.save(existingMovie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(movie, null);

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
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
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
