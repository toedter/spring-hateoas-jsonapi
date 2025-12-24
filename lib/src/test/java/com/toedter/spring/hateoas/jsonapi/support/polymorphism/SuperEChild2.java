/*
 * Copyright 2025 the original author or authors.
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

package com.toedter.spring.hateoas.jsonapi.support.polymorphism;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiType;
import lombok.NoArgsConstructor;

// tag::SuperEChild2[]
@NoArgsConstructor
public class SuperEChild2 implements SuperEntity<String> {

  @JsonApiId private String id;

  @JsonApiType private final String type = null;

  private final String extraAttribute = "";

  @Override
  public String getT() {
    return null;
  }
}
// end::SuperEChild2[]
