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

import com.toedter.spring.hateoas.jsonapi.JsonApiMeta;
import com.toedter.spring.hateoas.jsonapi.JsonApiType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// tag::Movie[]
// @fold:on
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// @fold:off
public class MovieWithAnnotations {

  @Id
  private String myId;

  @JsonApiType
  private String type;

  @JsonApiMeta
  private String myMeta;

  private String title;
}
// end::Movie[]
