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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

/**
 * Collection of JSON:API error objects for building compliant error responses.
 *
 * <p>This class represents the top-level {@code errors} member of a JSON:API document, containing
 * one or more {@link JsonApiError} objects. According to the JSON:API specification, a document
 * must contain either {@code data}, {@code errors}, or {@code meta} at the top level, and {@code
 * errors} must not coexist with {@code data}.
 *
 * <p><b>Usage example:</b>
 *
 * <pre>{@code
 * JsonApiErrors errors = JsonApiErrors.create()
 *   .withError(JsonApiError.create()
 *     .withStatus("404")
 *     .withTitle("Resource not found"))
 *   .withError(JsonApiError.create()
 *     .withStatus("403")
 *     .withTitle("Forbidden"));
 * }</pre>
 *
 * @author Kai Toedter
 * @see JsonApiError
 * @see <a href="https://jsonapi.org/format/#errors">JSON:API Error Objects</a>
 */
@ToString
public class JsonApiErrors {

  /**
   * Gets the list of {@link JsonApiError} objects contained in this errors collection.
   *
   * @return an unmodifiable view of the error list; will never be {@literal null}
   */
  @Getter private final List<JsonApiError> errors = new ArrayList<>();

  /**
   * Creates an empty {@link JsonApiErrors} collection.
   *
   * <p>Use {@link #withError(JsonApiError)} to add errors to the collection.
   */
  public JsonApiErrors() {}

  /**
   * Creates a {@link JsonApiErrors} collection with an initial error.
   *
   * @param jsonApiError the initial error to add; must not be {@literal null}
   * @throws NullPointerException if {@code jsonApiError} is {@literal null}
   */
  public JsonApiErrors(JsonApiError jsonApiError) {
    errors.add(jsonApiError);
  }

  /**
   * Adds an error to this collection.
   *
   * <p>This method returns {@code this} to support fluent method chaining.
   *
   * @param jsonApiError the error to add; must not be {@literal null}
   * @return this {@link JsonApiErrors} instance for method chaining
   * @throws NullPointerException if {@code jsonApiError} is {@literal null}
   */
  public JsonApiErrors withError(JsonApiError jsonApiError) {
    errors.add(jsonApiError);
    return this;
  }

  /**
   * Creates a new empty {@link JsonApiErrors} collection.
   *
   * <p>This factory method is provided for consistency with other builder patterns in the library.
   *
   * @return a new empty {@link JsonApiErrors} instance
   */
  public static JsonApiErrors create() {
    return new JsonApiErrors();
  }
}
