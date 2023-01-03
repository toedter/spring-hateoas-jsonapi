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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiErrors Test")
class JsonApiErrorsIntegrationTest extends JsonApiTestBase {
    private ObjectMapper mapper;

    @BeforeEach
    void setUpModule() {
        JsonApiMediaTypeConfiguration configuration = new JsonApiMediaTypeConfiguration(null, null);
        mapper = new ObjectMapper();
        configuration.configureObjectMapper(mapper, new JsonApiConfiguration());
        mapper.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
    }

    @Test
    void should_build_empty_errors() throws Exception {
        JsonApiErrors jsonApiErrors = new JsonApiErrors();
        String errorsJson = mapper.writeValueAsString(jsonApiErrors);
        assertThat(errorsJson).isEqualTo("{\"errors\":[]}");
    }

    @Test
    void should_build_single_error() throws Exception {
        JsonApiError jsonApiError = new JsonApiError().withId("123").withCode("404");
        JsonApiErrors jsonApiErrors = new JsonApiErrors(jsonApiError);
        String errorsJson = mapper.writeValueAsString(jsonApiErrors);
        compareWithFile(errorsJson, "errorsSingle.json");
    }

    @Test
    void should_build_two_errors() throws Exception {
        JsonApiError jsonApiError = new JsonApiError().withId("123").withCode("456");
        JsonApiErrors jsonApiErrors = new JsonApiErrors(jsonApiError);
        jsonApiErrors.withError(new JsonApiError().withId("345").withDetail("error details"));
        String errorsJson = mapper.writeValueAsString(jsonApiErrors);
        compareWithFile(errorsJson, "errorsMany.json");
    }

    @Test
    void should_build_complete_error() throws Exception {
        HashMap<String, Object> metaMap = new HashMap<>();
        metaMap.put("foo", "bar");
        JsonApiError jsonApiError = new JsonApiError()
                .withId("1")
                .withAboutLink("http://example-error/about")
                .withStatus("500")
                .withCode("404")
                .withTitle("error title")
                .withDetail("error detail")
                .withSourcePointer("to infinity and beyond")
                .withSourceParameter("...but always with towel.")
                .withMeta(metaMap);
        JsonApiErrors jsonApiErrors = new JsonApiErrors(jsonApiError);
        String errorsJson = mapper.writeValueAsString(jsonApiErrors);
        compareWithFile(errorsJson, "errorsAllAttributes.json");
    }

    @Test
    void should_build_complete_error_with_other_invocation_order() throws Exception {
        HashMap<String, Object> metaMap = new HashMap<>();
        metaMap.put("foo", "bar");
        JsonApiError jsonApiError = new JsonApiError()
                .withId("1")
                .withAboutLink("http://example-error/about")
                .withStatus("500")
                .withCode("404")
                .withTitle("error title")
                .withDetail("error detail")
                .withSourceParameter("...but always with towel.")
                .withSourcePointer("to infinity and beyond")
                .withMeta(metaMap);
        JsonApiErrors jsonApiErrors = new JsonApiErrors(jsonApiError);
        String errorsJson = mapper.writeValueAsString(jsonApiErrors);
        compareWithFile(errorsJson, "errorsAllAttributes.json");
    }

}
