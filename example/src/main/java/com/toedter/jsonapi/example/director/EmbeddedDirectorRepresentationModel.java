package com.toedter.jsonapi.example.director;

import lombok.Data;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

import java.util.Arrays;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Data
public class EmbeddedDirectorRepresentationModel extends EntityModel<Director> {
    public EmbeddedDirectorRepresentationModel(Director director) {
        super(director, linkTo(methodOn(DirectorController.class).findOne(director.getId())).withSelfRel());
    }

    public EmbeddedDirectorRepresentationModel(Director director, Link... links) {
        super(director, Arrays.asList(links));
    }
}
