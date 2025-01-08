package com.toedter.spring.hateoas.jsonapi.example.exception;

import com.toedter.spring.hateoas.jsonapi.JsonApiError;

// tag::common-errors[]
public class CommonErrors {

  private CommonErrors() {}

  private static final JsonApiError resourceNotFound = JsonApiError.create()
    .withCode("xrn:err:platform:resourceNotFound")
    .withTitle("Resource Not Found")
    .withStatus("404");

  private static final JsonApiError badRequest = JsonApiError.create()
    .withCode("xrn:err:platform:badRequest")
    .withTitle("Bad Request")
    .withStatus("400");

  private static final JsonApiError internalServerError = JsonApiError.create()
    .withCode("xrn:err:platform:internalServerError")
    .withTitle("Internal server error")
    .withStatus("500");

  public static JsonApiError newResourceNotFound(
    String resourceType,
    String resourceId
  ) {
    return resourceNotFound
      .withDetail(
        "Resource of type '" +
        resourceType +
        "' with id '" +
        resourceId +
        "' not found."
      )
      .withId(java.util.UUID.randomUUID().toString());
  }

  public static JsonApiError newBadRequestError(String message) {
    return badRequest
      .withId(java.util.UUID.randomUUID().toString())
      .withDetail(message);
  }

  public static JsonApiError newInternalServerError() {
    return internalServerError.withId(java.util.UUID.randomUUID().toString());
  }
}
// end::common-errors[]
