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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiMediaTypeConfiguration Unit Test")
class JsonApiMediaTypeConfigurationUnitTest {

  private JsonApiMediaTypeConfiguration configuration;

  @BeforeEach
  void setUpModule() {
    configuration = new JsonApiMediaTypeConfiguration(null, null);
  }

  @Test
  void should_return_json_api_media_type() {
    List<MediaType> mediaTypes = configuration.getMediaTypes();
    assertThat(mediaTypes).hasSize(1);
    assertThat(mediaTypes.get(0)).hasToString("application/vnd.api+json");
  }

  // Note: getJacksonModule() test removed - Jackson 3 uses configureJsonMapper(Builder) instead

  @Test
  void should_return_configured_object_mapper() {
    JsonMapper.Builder builder = JsonMapper.builder();
    builder = configuration.configureJsonMapper(builder);
    JsonMapper jsonMapper = new JsonApiConfiguration()
      .customize(builder)
      .build();

    assertThat(
      jsonMapper.isEnabled(
        SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED
      )
    ).isFalse();
    assertThat(
      jsonMapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    ).isFalse();
    assertThat(
      jsonMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    ).isFalse();
    // Note: getRegisteredModuleIds() doesn't exist in Jackson 3
  }
}
