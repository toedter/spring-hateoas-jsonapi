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
 * Marks a field or method as the source of the JSON:API resource identifier.
 *
 * <p>This annotation can be applied to fields or getter methods to explicitly specify which
 * property should be used as the {@code id} member of a JSON:API resource object. The annotated
 * field or method return value will be serialized as the resource's {@code id}.
 *
 * <p>If this annotation is not present, the library will fall back to:
 *
 * <ol>
 *   <li>JPA {@code @Id} annotation (if present)
 *   <li>A field named "id"
 * </ol>
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * public class Movie {
 *   @JsonApiId
 *   private String movieIdentifier;
 *   // or on getter:
 *   @JsonApiId
 *   public String getMovieIdentifier() {
 *     return movieIdentifier;
 *   }
 * }
 * }</pre>
 *
 * @author Kai Toedter
 * @see JsonApiType
 * @see <a href="https://jsonapi.org/format/#document-resource-object-identification">JSON:API
 *     Resource Identification</a>
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface JsonApiId {}
