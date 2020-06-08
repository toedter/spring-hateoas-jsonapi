package com.toedter.jsonapi.example.director;

import com.toedter.jsonapi.example.movie.Movie;
import com.toedter.jsonapi.example.movie.MovieController;
import lombok.Data;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
@Relation(collectionRelation = "directors")
public class DirectorRepresentationModel extends RepresentationModel<DirectorRepresentationModel> {
    private String name;

    public DirectorRepresentationModel(Director director) {
        this.name = director.getName();

        add(linkTo(methodOn(DirectorController.class).findOne(director.getId())).withSelfRel());

        for (Movie movie : director.getMovies()) {
            add(linkTo(methodOn(MovieController.class)
                    .findOne(movie.getId()))
                    .withRel("movies")
                    .withTitle(movie.getTitle()));
        }
    }
}
