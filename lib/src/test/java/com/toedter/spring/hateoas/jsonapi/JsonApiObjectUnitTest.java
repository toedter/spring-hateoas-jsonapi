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

import java.net.URI;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiObject Unit Test")
class JsonApiObjectUnitTest {

  @Test
  void should_create_null_version_with_single_argument_constructor() {
    JsonApiObject jsonApiObject = new JsonApiObject(false);
    assertThat(jsonApiObject.getVersion()).isNull();
  }

  @Test
  void should_create_version_with_single_argument_constructor() {
    JsonApiObject jsonApiObject = new JsonApiObject(true);
    assertThat(jsonApiObject.getVersion()).isEqualTo("1.1");
    assertThat(jsonApiObject.getExt()).isNull();
    assertThat(jsonApiObject.getProfile()).isNull();
    assertThat(jsonApiObject.getMeta()).isNull();
  }

  @Test
  void should_create_empty_object_with_no_args_constructor() {
    JsonApiObject jsonApiObject = new JsonApiObject();
    assertThat(jsonApiObject.getVersion()).isNull();
    assertThat(jsonApiObject.getExt()).isNull();
    assertThat(jsonApiObject.getProfile()).isNull();
    assertThat(jsonApiObject.getMeta()).isNull();
  }

  @Test
  void should_create_full_object_with_all_args_constructor() {
    List<URI> extensions = List.of(
      URI.create("https://jsonapi.org/ext/atomic")
    );
    List<URI> profiles = List.of(
      URI.create("http://example.com/profiles/flexible-pagination")
    );
    Map<String, Object> meta = Map.of("copyright", "Copyright 2025");

    JsonApiObject jsonApiObject = new JsonApiObject(
      true,
      extensions,
      profiles,
      meta
    );

    assertThat(jsonApiObject.getVersion()).isEqualTo("1.1");
    assertThat(jsonApiObject.getExt()).isEqualTo(extensions);
    assertThat(jsonApiObject.getProfile()).isEqualTo(profiles);
    assertThat(jsonApiObject.getMeta()).isEqualTo(meta);
  }

  @Test
  void should_create_object_with_version_only() {
    JsonApiObject jsonApiObject = new JsonApiObject(true, null, null, null);
    assertThat(jsonApiObject.getVersion()).isEqualTo("1.1");
    assertThat(jsonApiObject.getExt()).isNull();
    assertThat(jsonApiObject.getProfile()).isNull();
    assertThat(jsonApiObject.getMeta()).isNull();
  }

  @Test
  void should_create_object_without_version() {
    List<URI> extensions = List.of(
      URI.create("https://jsonapi.org/ext/atomic")
    );
    JsonApiObject jsonApiObject = new JsonApiObject(
      false,
      extensions,
      null,
      null
    );
    assertThat(jsonApiObject.getVersion()).isNull();
    assertThat(jsonApiObject.getExt()).isEqualTo(extensions);
    assertThat(jsonApiObject.getProfile()).isNull();
    assertThat(jsonApiObject.getMeta()).isNull();
  }
}
