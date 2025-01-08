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

package com.toedter.spring.hateoas.jsonapi;

import org.springframework.http.MediaType;

/**
 * Provides the {@literal JSON:API} media type {@code application/vnd.api+json}.
 */
public class MediaTypes {

  private MediaTypes() {}

  /**
   * A String equivalent of the JSON:API media type
   */
  public static final String JSON_API_VALUE = "application/vnd.api+json";

  /**
   * Public constant media type for {@code application/vnd.api+json}.
   */
  public static final MediaType JSON_API = MediaType.valueOf(JSON_API_VALUE);
}
