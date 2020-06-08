package com.toedter.jsonapi.example;

import com.toedter.jsonapi.example.director.DirectorController;
import com.toedter.jsonapi.example.movie.MovieController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
public class RootController {

	public static final String API_BASE_PATH = "/api";

	@GetMapping(API_BASE_PATH)
    ResponseEntity<RepresentationModel> root() {

		RepresentationModel resourceSupport = new RepresentationModel();

		resourceSupport.add(linkTo(methodOn(RootController.class).root()).withSelfRel());

		Link selfLink = linkTo(MovieController.class).slash("movies").withRel("movies");
		Link templatedLink = new Link(selfLink.getHref() + "{?size,page}").withRel("movies");

		resourceSupport.add(templatedLink);

		selfLink = linkTo(DirectorController.class).slash("directors").withRel("directors");
		templatedLink = new Link(selfLink.getHref() + "{?size,page}").withRel("directors");

		resourceSupport.add(templatedLink);

		return ResponseEntity.ok(resourceSupport);
	}

}
