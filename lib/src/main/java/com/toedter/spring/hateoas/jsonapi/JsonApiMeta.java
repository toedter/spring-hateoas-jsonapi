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
 * Marks a field or method for inclusion in the JSON:API {@code meta} object.
 *
 * <p>Fields or setter/getter methods annotated with {@code @JsonApiMeta} will be serialized to and
 * deserialized from the {@code meta} member of a JSON:API resource object. The {@code meta} object
 * can contain any non-standard information about a resource.
 *
 * <p>During serialization, the annotated field value becomes a property in the {@code meta} object.
 * During deserialization, values from the {@code meta} object are mapped back to the annotated
 * fields.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * public class Movie {
 *   private String title;
 *
 *   @JsonApiMeta
 *   private String copyright;
 *
 *   @JsonApiMeta
 *   private int viewCount;
 * }
 *
 * // Results in JSON:API:
 * {
 *   "data": {
 *     "type": "movies",
 *     "id": "1",
 *     "attributes": {
 *       "title": "The Matrix"
 *     },
 *     "meta": {
 *       "copyright": "Warner Bros",
 *       "viewCount": 1000000
 *     }
 *   }
 * }
 * }</pre>
 *
 * @author Kai Toedter
 * @see <a href="https://jsonapi.org/format/#document-meta">JSON:API Meta Information</a>
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JsonApiMeta {}
