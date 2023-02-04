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

import com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder;
import com.toedter.spring.hateoas.jsonapi.example.RootController;
import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.example.director.DirectorRepository;
import javax.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static com.toedter.spring.hateoas.jsonapi.JsonApiModelBuilder.jsonApiModel;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.DIRECTORS;
import static com.toedter.spring.hateoas.jsonapi.example.MoviesDemoApplication.MOVIES;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class MovieController {

    private final MovieRepository movieRepository;
    private final DirectorRepository directorRepository;
    private final MovieModelAssembler movieModelAssembler;

    MovieController(MovieRepository movieRepository, DirectorRepository directorRepository,
                    MovieModelAssembler movieModelAssembler) {
        this.movieRepository = movieRepository;
        this.directorRepository = directorRepository;
        this.movieModelAssembler = movieModelAssembler;
    }

    @GetMapping("/movies")
    public ResponseEntity<RepresentationModel<?>> findAll(
            @RequestParam(value = "page[number]", defaultValue = "0", required = false) int page,
            @RequestParam(value = "page[size]", defaultValue = "10", required = false) int size,
            @RequestParam(value = "include", required = false) String[] include,
            @RequestParam(value = "fields[" + MOVIES + "]", required = false) String[] fieldsMovies) {

        final PageRequest pageRequest = PageRequest.of(page, size);

        final Page<Movie> pagedResult = movieRepository.findAll(pageRequest);

        List<? extends RepresentationModel<?>> movieResources =
                StreamSupport.stream(pagedResult.spliterator(), false)
                        .map(movie -> movieModelAssembler.toJsonApiModel(movie, fieldsMovies))
                        .toList();

        String uriParams = "?";
        if (fieldsMovies != null) {

            String movieFieldParams = Arrays.toString(fieldsMovies);
            uriParams = "?fields%5Bmovies%5D=" +
                    movieFieldParams.substring(1, movieFieldParams.length() - 1).replace(" ", "");
        }

        if (include != null) {
            String includeParams = Arrays.toString(include);
            if (!uriParams.equals("?")) {
                uriParams += "&";
            }
            uriParams += "include=" +
                    includeParams.substring(1, includeParams.length() - 1).replace(" ", "");
        }

        // tag::affordance[]
        final Affordance newMovieAffordance =
                afford(methodOn(MovieController.class).newMovie(null));

        Link selfLink = linkTo(MovieController.class).slash("movies" + uriParams
                + "&page%5Bnumber%5D=" + pagedResult.getNumber()
                + "&page%5Bsize%5D=" + pagedResult.getSize()).withSelfRel().andAffordance(newMovieAffordance);
        // end::affordance[]

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(
                        pagedResult.getSize(),
                        pagedResult.getNumber(),
                        pagedResult.getTotalElements(),
                        pagedResult.getTotalPages());

        final PagedModel<? extends RepresentationModel<?>> pagedModel =
                PagedModel.of(movieResources, pageMetadata);

        if (uriParams.equals("?")) {
            uriParams = "";
        }

        String pageLinksBase =
                linkTo(MovieController.class).slash(MOVIES).withSelfRel().getHref() + uriParams;

        final JsonApiModelBuilder jsonApiModelBuilder =
                jsonApiModel().model(pagedModel).link(selfLink).pageLinks(pageLinksBase);

        // tag::relationship-inclusion[]
        if (include != null && include.length == 1 && include[0].equals(DIRECTORS)) {
            for (Movie movie : pagedResult.getContent()) {
                jsonApiModelBuilder.included(movie.getDirectors());
            }
        }
        // end::relationship-inclusion[]

        final RepresentationModel<?> pagedJsonApiModel = jsonApiModelBuilder.build();

        return ResponseEntity.ok(pagedJsonApiModel);
    }

    // tag::new-movie[]
    @PostMapping("/movies")
    public ResponseEntity<?> newMovie(@RequestBody EntityModel<Movie> movieModel) {
        // end::new-movie[]
        Movie movie = movieModel.getContent();
        assert movie != null;
        movieRepository.save(movie);

        List<Director> directorsWithIds = movie.getDirectors();
        List<Director> directors = new ArrayList<>();
        movie.setDirectors(directors);

        for (Director directorWithId : directorsWithIds) {
            directorRepository.findById(directorWithId.getId()).map(director -> {
                director.addMovie(movie);
                directorRepository.save(director);
                movie.addDirector(director);
                return director;
            });
        }
        movieRepository.save(movie);

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
                .map(uri -> ResponseEntity.created(uri).body(movieRepresentationModel))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to create " + movie));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<? extends RepresentationModel<?>> findOne(
            @PathVariable Long id,
            @RequestParam(value = "include", required = false) String[] include,
            @RequestParam(value = "fields[" + MOVIES + "]", required = false) String[] filterMovies) {

        return movieRepository.findById(id)
                .map(movie -> setInclude(movie, include, filterMovies))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private RepresentationModel<?> setInclude(Movie movie, String[] include, String[] filterMovies) {
        RepresentationModel<?> model = movieModelAssembler.toJsonApiModel(movie, filterMovies);
        JsonApiModelBuilder builder = jsonApiModel().model(model);
        if (include != null && include.length == 1 && include[0].equals(DIRECTORS)) {
            movie.getDirectors().forEach(entry -> builder.included(EntityModel.of(entry)));
        }
        return builder.build();
    }

    @GetMapping("/movies/{id}/directors")
    public ResponseEntity<? extends RepresentationModel<?>> findDirectors(@PathVariable Long id) {

        return movieRepository.findById(id)
                .map(movieModelAssembler::directorsToJsonApiModel)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/movies/{id}")
    public ResponseEntity<?> updateMoviePartially(@RequestBody EntityModel<Movie> movieModel, @PathVariable Long id) {

        Movie existingMovie = movieRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(id.toString()));
        Movie movie = movieModel.getContent();
        existingMovie.update(movie);

        movieRepository.save(existingMovie);
        final RepresentationModel<?> movieRepresentationModel = movieModelAssembler.toJsonApiModel(existingMovie, null);

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
        Optional<Movie> optional = movieRepository.findById(id);
        if (optional.isPresent()) {
            Movie movie = optional.get();
            for (Director director : movie.getDirectors()) {
                director.deleteMovie(movie);
            }
            movieRepository.deleteById(id);
        }

        return ResponseEntity.noContent().build();
    }
}
