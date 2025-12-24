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

package com.toedter.spring.hateoas.jsonapi.example.exception;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;
import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// tag::jsonapi-errors-exception[]
@Getter
public class JsonApiErrorsException extends RuntimeException {

  private final transient JsonApiErrors errors;
  private final HttpStatus status;

  public JsonApiErrorsException(JsonApiErrors errors, HttpStatus status) {
    super();
    this.errors = errors;
    this.status = status;
  }

  public JsonApiErrorsException(JsonApiError error) {
    super();
    this.errors = JsonApiErrors.create().withError(error);
    this.status = HttpStatus.valueOf(Integer.parseInt(error.getStatus()));
  }
}
// end::jsonapi-errors-exception[]
