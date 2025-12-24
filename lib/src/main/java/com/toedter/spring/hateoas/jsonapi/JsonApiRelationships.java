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

package com.toedter.spring.hateoas.jsonapi;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the JSON:API relationships of a resource object. This annotation is only used for
 * deserialization of JSON:API structured JSON to Java objects, and is useful for POST and PATCH
 * requests. For serialization use a {@link JsonApiModelBuilder}.
 *
 * @author Kai Toedter
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JsonApiRelationships {
  /**
   * The name of the relationship.
   *
   * @return the name of the relationship
   */
  String value();
}
