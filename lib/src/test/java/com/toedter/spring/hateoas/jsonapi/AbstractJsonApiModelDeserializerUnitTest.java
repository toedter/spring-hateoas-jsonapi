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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiConfiguration Unit Test")
class AbstractJsonApiModelDeserializerUnitTest {

  class TestJsonApiDeserializer
    extends AbstractJsonApiModelDeserializer<EntityModel<?>> {

    public TestJsonApiDeserializer(JsonApiConfiguration jsonApiConfiguration) {
      super(jsonApiConfiguration);
    }

    public TestJsonApiDeserializer(
      JavaType contentType,
      JsonApiConfiguration jsonApiConfiguration
    ) {
      super(contentType, jsonApiConfiguration);
    }

    @Override
    protected EntityModel<?> convertToRepresentationModel(
      List<Object> resources,
      JsonApiDocument doc
    ) {
      return null;
    }

    @Override
    protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
      return null;
    }
  }

  TestJsonApiDeserializer testJsonApiDeserializer;

  @BeforeEach
  void beforeEach() {
    testJsonApiDeserializer = new TestJsonApiDeserializer(
      new JsonApiConfiguration()
    );
  }

  @Test
  void should_get_content_type() {
    JavaType contentType = testJsonApiDeserializer.getContentType();
    assertThat(contentType.getRawClass()).isEqualTo(JsonApiDocument.class);
  }

  @Test
  void should_get_null_content_deserializer() {
    JsonDeserializer<Object> contentDeserializer =
      testJsonApiDeserializer.getContentDeserializer();
    assertThat(contentDeserializer).isNull();
  }

  @Test
  void should_not_convert_to_resource() {
    HashMap<String, Object> data = new HashMap<>();
    data.put("test-key", "test-value");

    class TestWitNoConstructor {}

    JavaType javaType = TypeFactory.defaultInstance().constructSimpleType(
      TestWitNoConstructor.class,
      new JavaType[0]
    );
    testJsonApiDeserializer = new TestJsonApiDeserializer(
      javaType,
      new JsonApiConfiguration()
    );

    Assertions.assertThrows(IllegalStateException.class, () ->
      testJsonApiDeserializer.convertToResource(data, false, null, null, false)
    );
  }
}
