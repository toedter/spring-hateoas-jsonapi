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

import dev.harrel.json.providers.jackson3.Jackson3Node;
import dev.harrel.jsonschema.Error;
import dev.harrel.jsonschema.JsonNodeFactory;
import dev.harrel.jsonschema.Validator;
import dev.harrel.jsonschema.ValidatorFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kai Toedter
 */
public abstract class JsonApiTestBase {

  void compareWithFile(String json, String fileName) throws Exception {
    compareWithFile(json, fileName, true);
  }

  void compareWithFile(String json, String fileName, boolean validateSchema)
    throws Exception {
    File file = new ClassPathResource(fileName, getClass()).getFile();
    JsonMapper jsonMapper = JsonMapper.builder().build();
    JsonNode expectedJsonNode = jsonMapper.readValue(file, JsonNode.class);
    JsonNode actualJsonNode = jsonMapper.readValue(json, JsonNode.class);
    assertThat(actualJsonNode).isEqualTo(expectedJsonNode);

    if (validateSchema) {
      // Load the JSON schema from resources
      InputStream schemaStream = new ClassPathResource("jsonapi-schema.json", getClass()).getInputStream();
      String schemaString = new String(schemaStream.readAllBytes());

      JsonNodeFactory factory = new Jackson3Node.Factory();
      Validator validator = new ValidatorFactory()
              .withJsonNodeFactory(factory)
              .createValidator();

      tools.jackson.databind.JsonNode providerSchemaNode = new JsonMapper().readTree(schemaString);
      URI schemaUri = validator.registerSchema(providerSchemaNode);

      Validator.Result validatenResult = validator.validate(schemaUri, actualJsonNode);
      if(!validatenResult.isValid()) {
        for (Error error : validatenResult.getErrors()) {
          System.out.println("Validation error: " + error.getError());
        }
      }
      assertThat(validatenResult.isValid()).isTrue();
    }
  }

  String readFile(String fileName) throws IOException {
    File file = new ClassPathResource(fileName, getClass()).getFile();
    JsonMapper jsonMapper = JsonMapper.builder().build();
    return jsonMapper.readValue(file, JsonNode.class).toString();
  }

  JsonNode readFileAsJsonNode(String fileName) throws IOException {
    File file = new ClassPathResource(fileName, getClass()).getFile();
    JsonMapper jsonMapper = JsonMapper.builder().build();
    return jsonMapper.readValue(file, JsonNode.class);
  }

  InputStream getStream(String fileName) throws IOException {
    return new ClassPathResource(fileName, getClass()).getInputStream();
  }

  JsonMapper createJsonMapper(JsonApiConfiguration jsonApiConfiguration) {
    // Create ObjectProvider that supplies the given JsonApiConfiguration
    ObjectProvider<JsonApiConfiguration> configProvider =
      new ObjectProvider<>() {
        @Override
        public JsonApiConfiguration getObject() {
          return jsonApiConfiguration;
        }
      };

    JsonApiMediaTypeConfiguration configuration =
      new JsonApiMediaTypeConfiguration(configProvider, null);
    JsonMapper.Builder builder = JsonMapper.builder();
    builder = configuration.configureJsonMapper(builder);
    return builder.build();
  }
}
