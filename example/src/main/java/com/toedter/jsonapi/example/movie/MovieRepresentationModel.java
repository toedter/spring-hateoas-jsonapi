package com.toedter.jsonapi.example.movie;

import com.toedter.jsonapi.example.director.Director;
import com.toedter.jsonapi.example.director.DirectorController;
import lombok.Data;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.Arrays;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
public class MovieRepresentationModel extends EntityModel<Movie> {
     public MovieRepresentationModel(Movie movie) {
        super(movie, linkTo(methodOn(MovieController.class).findOne(movie.getId())).withSelfRel());
        initializeDirectors(movie);
    }

    public MovieRepresentationModel(Movie movie, Link... links) {
        super(movie, Arrays.asList(links));
        initializeDirectors(movie);
    }

    private void initializeDirectors(Movie movie) {
        for (Director director : movie.getDirectors()) {
            add(linkTo(methodOn(DirectorController.class)
                    .findOne(director.getId()))
                    .withRel("directors")
                    .withName(director.getName()));
        }
    }

}
