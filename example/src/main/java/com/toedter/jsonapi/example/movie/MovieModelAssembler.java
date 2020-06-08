package com.toedter.jsonapi.example.movie;

import com.toedter.jsonapi.example.director.Director;
import com.toedter.spring.hateoas.jsonapi.JsonApiResourceModelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.stereotype.Component;

import static com.toedter.spring.hateoas.jsonapi.JsonApiResourceModelBuilder.jsonApiModel;
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

    public RepresentationModel<?> toJsonApiModel(Movie movie) {
        Link selfLink = linkTo(methodOn(MovieController.class).findOne(movie.getId())).withSelfRel();

        Link moviesLink = linkTo(MovieController.class).slash("movies").withRel("movies");
        Link templatedMoviesLink = Link.of(moviesLink.getHref() + "{?page[number],page[size]}").withRel("movies");

        final MovieRepresentationModel movieRepresentationModel =
                new MovieRepresentationModel(movie, selfLink, templatedMoviesLink);

        JsonApiResourceModelBuilder builder = jsonApiModel()
                .entity(movieRepresentationModel.getContent())
                .links(movieRepresentationModel.getLinks());
        for (Director director : movie.getDirectors()) {
            EntityModel<Director> directorEntityModel = EntityModel.of(director);
            builder = builder.relationship("directors", directorEntityModel);
        }

        final RepresentationModel<?> jsonApiModel = builder.build();
        return jsonApiModel;
    }

}
