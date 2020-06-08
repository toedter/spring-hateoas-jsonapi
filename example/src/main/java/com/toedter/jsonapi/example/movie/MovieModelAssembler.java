package com.toedter.jsonapi.example.movie;

import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
@Slf4j
class MovieModelAssembler {

    public MovieRepresentationModel toModel(Movie movie) {
        Link selfLink = linkTo(methodOn(MovieController.class).findOne(movie.getId())).withSelfRel();

        Link moviesLink = linkTo(MovieController.class).slash("movies").withRel("movies");
        Link templatedMoviesLink = new Link(moviesLink.getHref() + "{?size,page}").withRel("movies");

        final Affordance updateAffordance =
                afford(methodOn(MovieController.class).updateMovie(movie, movie.getId()));

        final Affordance updatePartiallyAffordance =
                afford(methodOn(MovieController.class).updateMoviePartially(movie, movie.getId()));

        final Affordance deleteAffordance =
                afford(methodOn(MovieController.class).deleteMovie(movie.getId()));

        return new MovieRepresentationModel(movie, selfLink
                .andAffordance(updateAffordance)
                .andAffordance(updatePartiallyAffordance)
                .andAffordance(deleteAffordance),
                templatedMoviesLink);
    }
}
