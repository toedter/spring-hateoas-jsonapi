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

package com.toedter.spring.hateoas.jsonapi.example;

import com.toedter.spring.hateoas.jsonapi.example.director.DirectorController;
import com.toedter.spring.hateoas.jsonapi.example.movie.MovieController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API_VALUE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(value = RootController.API_BASE_PATH, produces = JSON_API_VALUE)
public class RootController {

	public static final String API_BASE_PATH = "/api";

	@GetMapping
    ResponseEntity<RepresentationModel<?>> root() {

		RepresentationModel<?> resourceSupport = new RepresentationModel<>();

		resourceSupport.add(linkTo(methodOn(RootController.class).root()).withSelfRel());

		Link selfLink = linkTo(MovieController.class).slash("movies").withRel("movies");
		Link templatedLink = Link.of(selfLink.getHref() + "{?page[number],page[size]}").withRel("movies");

		resourceSupport.add(templatedLink);

		selfLink = linkTo(DirectorController.class).slash("directors").withRel("directors");
		templatedLink = Link.of(selfLink.getHref() + "{?page[number],page[size]}").withRel("directors");

		resourceSupport.add(templatedLink);

		return ResponseEntity.ok(resourceSupport);
	}

}
