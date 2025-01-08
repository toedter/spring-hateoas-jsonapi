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

package com.toedter.spring.hateoas.jsonapi.support.polymorphism;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiType;
import java.util.Collection;
import lombok.NoArgsConstructor;

// tag::SuperEChild[]
@NoArgsConstructor
public class SuperEChild<T extends Collection<?>> implements SuperEntity<T> {

  @JsonApiId
  private String id;

  @JsonApiType
  private final String type = null;

  @Override
  public T getT() {
    return null;
  }
}
// end::SuperEChild[]
