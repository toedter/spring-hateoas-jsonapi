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

package com.toedter.spring.hateoas.jsonapi.support;

import com.toedter.spring.hateoas.jsonapi.JsonApiType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.RepresentationModel;

@Getter
@Setter
public class MovieRepresentationModelWithJsonApiType
  extends RepresentationModel<MovieRepresentationModelWithJsonApiType> {

  private String id;

  @JsonApiType
  private String type;

  private String name;

  public MovieRepresentationModelWithJsonApiType() {}

  public MovieRepresentationModelWithJsonApiType(Movie movie) {
    this.id = movie.getId();
    this.name = movie.getTitle();
    this.type = "movie-type";
    add(Links.of(Link.of("http://localhost/movies/7").withSelfRel()));
  }
}
