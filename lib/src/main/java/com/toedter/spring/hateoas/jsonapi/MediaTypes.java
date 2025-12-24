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

import org.springframework.http.MediaType;

/**
 * Provides constants for the JSON:API media type.
 *
 * <p>This utility class defines the official JSON:API media type {@code application/vnd.api+json}
 * as specified in the JSON:API specification. These constants can be used for content negotiation,
 * request/response type declarations, and Spring MVC mapping annotations.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * @GetMapping(produces = MediaTypes.JSON_API_VALUE)
 * public EntityModel<Movie> getMovie() {
 *   // ...
 * }
 *
 * @RequestMapping(consumes = MediaTypes.JSON_API_VALUE)
 * public void createMovie(@RequestBody Movie movie) {
 *   // ...
 * }
 * }</pre>
 *
 * @author Kai Toedter
 * @see <a href="https://jsonapi.org/format/#content-negotiation">JSON:API Content Negotiation</a>
 */
public class MediaTypes {

  private MediaTypes() {}

  /**
   * String representation of the JSON:API media type: {@value}.
   *
   * <p>Use this constant with annotations like {@code @RequestMapping}, {@code @GetMapping}, etc.
   */
  public static final String JSON_API_VALUE = "application/vnd.api+json";

  /**
   * {@link MediaType} constant for JSON:API: {@code application/vnd.api+json}.
   *
   * <p>Use this constant for programmatic media type handling and content negotiation with Spring's
   * {@link MediaType} API.
   */
  public static final MediaType JSON_API = MediaType.valueOf(JSON_API_VALUE);
}
