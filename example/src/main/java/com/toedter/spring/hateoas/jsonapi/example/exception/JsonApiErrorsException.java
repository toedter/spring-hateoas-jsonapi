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
