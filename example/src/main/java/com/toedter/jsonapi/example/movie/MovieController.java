package com.toedter.jsonapi.example.movie;

import com.toedter.jsonapi.example.RootController;
import com.toedter.jsonapi.example.director.Director;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    ResponseEntity<CollectionModel<MovieRepresentationModel>> findAll(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {

        final int finalSize = size;
        final int finalPage = page;
        final PageRequest pageRequest = PageRequest.of(finalPage, finalSize);

        final Page<Movie> pagedResult = repository.findAll(pageRequest);

        List<MovieRepresentationModel> movieResources = StreamSupport.stream(pagedResult.spliterator(), false)
                .map(movie -> new MovieRepresentationModel(movie,
                        linkTo(methodOn(MovieController.class).findOne(movie.getId())).withSelfRel()))
                .collect(Collectors.toList());

        Link selfLink = linkTo(MovieController.class).slash("movies").withSelfRel();
        Link templatedLink = new Link(selfLink.getHref() + "{?size,page}").withSelfRel();

        final Affordance newMovieAffordance =
                afford(methodOn(MovieController.class).newMovie(null));

        PagedModel.PageMetadata pageMetadata =
                new PagedModel.PageMetadata(pagedResult.getSize(), pagedResult.getNumber(), pagedResult.getTotalElements(), pagedResult.getTotalPages());
        final PagedModel<MovieRepresentationModel> entityModels =
                new PagedModel<>(movieResources, pageMetadata, templatedLink.andAffordance(newMovieAffordance));

        final Pageable prev = pageRequest.previous();
        if (prev.getPageNumber() < page) {
            Link prevLink = new Link(selfLink.getHref() + "?page=" + prev.getPageNumber() + "&size=" + prev.getPageSize()).withRel(IanaLinkRelations.PREV);
            entityModels.add(prevLink);
        }

        final Pageable next = pageRequest.next();
        if (next.getPageNumber() > page && next.getPageNumber() < pagedResult.getTotalPages()) {
            Link nextLink = new Link(selfLink.getHref() + "?page=" + next.getPageNumber() + "&size=" + next.getPageSize()).withRel(IanaLinkRelations.NEXT);
            entityModels.add(nextLink);
        }

        if (page > 0) {
            Link firstLink = new Link(selfLink.getHref() + "?page=0&size=" + size).withRel(IanaLinkRelations.FIRST);
            entityModels.add(firstLink);
        }

        if (page < pagedResult.getTotalPages() - 1) {
            Link lastLink = new Link(selfLink.getHref() + "?page=" + (pagedResult.getTotalPages() - 1) + "&size=" + size).withRel(IanaLinkRelations.LAST);
            entityModels.add(lastLink);
        }

        return ResponseEntity.ok(entityModels);
    }

    @PostMapping("/movies")
    ResponseEntity<?> newMovie(@RequestBody Movie movie) {
        Movie savedMovie = repository.save(movie);
        MovieRepresentationModel movieRepresentationModel = movieModelAssembler.toModel(movie);

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
    public ResponseEntity<MovieRepresentationModel> findOne(@PathVariable Long id) {
        return repository.findById(id)
                .map(movie -> movieModelAssembler.toModel(movie))
                .map(ResponseEntity::ok) //
                .orElse(ResponseEntity.notFound().build());
    }

    private Link getMoviesLink() {
        Link moviesLink = linkTo(MovieController.class).slash("movies").withRel("movies");
        return new Link(moviesLink.getHref() + "{?size,page}").withRel("movies");
    }

    @PutMapping("/movies/{id}")
    ResponseEntity<?> updateMovie(@RequestBody Movie movie, @PathVariable Long id) {

        Movie movieToUpdate = movie;
        movieToUpdate.setId(id);

        repository.save(movieToUpdate);
        MovieRepresentationModel movieRepresentationModel = movieModelAssembler.toModel(movie);

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
                .orElse(ResponseEntity.badRequest().body("Unable to update " + movieToUpdate));
    }

    @PatchMapping("/movies/{id}")
    ResponseEntity<?> updateMoviePartially(@RequestBody Movie movie, @PathVariable Long id) {

        Movie existingMovie = repository.findById(id).orElseThrow(() -> new EntityNotFoundException(id.toString()));
        existingMovie.update(movie);

        repository.save(existingMovie);
        MovieRepresentationModel movieRepresentationModel = movieModelAssembler.toModel(existingMovie);

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
        if(optional.isPresent()) {
            Movie movie = optional.get();
            for(Director director: movie.getDirectors()) {
                director.deleteMovie(movie);
            }
            repository.deleteById(id);
        }

        return ResponseEntity.noContent().build();
    }
}
