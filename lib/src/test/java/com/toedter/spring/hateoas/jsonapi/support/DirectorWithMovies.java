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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiTypeForClass;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
@JsonApiTypeForClass("directors")
public class DirectorWithMovies {

  private String id;
  private String name;
  private int born;

  @JsonIgnore
  private List<MovieWithRating> movies;

  public DirectorWithMovies(String id, String name, int born) {
    this.id = id;
    this.name = name;
    this.born = born;
  }
}
