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

import com.toedter.spring.hateoas.jsonapi.JsonApiErrors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

// tag::exception-controller-advice[]
@ControllerAdvice(annotations = RestController.class)
@Slf4j
public class ExceptionControllerAdvice {

  /** Handles all manually thrown exceptions of type JsonApiErrorsException. */
  @ExceptionHandler
  public ResponseEntity<JsonApiErrors> handle(JsonApiErrorsException ex) {
    log.error("JSON:API error: Http status:{}, message:{}", ex.getStatus(), ex.getMessage());
    return ResponseEntity.status(ex.getStatus()).body(ex.getErrors());
  }

  /**
   * Handles all exceptions which are not of type JsonAPIErrorsException and treats them as internal
   * errors or maps specific exceptions to specific HTTP status codes.
   */
  @ExceptionHandler
  public ResponseEntity<JsonApiErrors> handle(Exception ex) {
    log.error("Internal error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            JsonApiErrors.create()
                .withError(CommonErrors.newInternalServerError().withDetail(ex.getMessage())));
  }
}
// end::exception-controller-advice[]
