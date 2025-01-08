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

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.IanaLinkRelations;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiConfiguration Unit Test")
class JsonApiConfigurationUnitTest {

  @Test
  void should_initialize_defaults() {
    assertThat(new JsonApiConfiguration().isPluralizedTypeRendered()).isTrue();
    assertThat(new JsonApiConfiguration().isLowerCasedTypeRendered()).isTrue();
    assertThat(new JsonApiConfiguration().isJsonApiVersionRendered()).isFalse();
    assertThat(
      new JsonApiConfiguration().isPageMetaAutomaticallyCreated()
    ).isTrue();
    assertThat(
      new JsonApiConfiguration().isTypeForClassUsedForDeserialization()
    ).isFalse();
    assertThat(
      new JsonApiConfiguration().isJsonApi11LinkPropertiesRemovedFromLinkMeta()
    ).isTrue();
    assertThat(
      new JsonApiConfiguration().getAffordancesRenderedAsLinkMeta()
    ).isEqualTo(JsonApiConfiguration.AffordanceType.NONE);
    assertThat(new JsonApiConfiguration().getLinksNotUrlEncoded()).isEmpty();
  }

  @Test
  void should_set_pluralized_type() {
    assertThat(
      new JsonApiConfiguration()
        .withPluralizedTypeRendered(false)
        .isPluralizedTypeRendered()
    ).isFalse();
  }

  @Test
  void should_set_lower_cased_type() {
    assertThat(
      new JsonApiConfiguration()
        .withLowerCasedTypeRendered(false)
        .isLowerCasedTypeRendered()
    ).isFalse();
  }

  @Test
  void should_set_render_version() {
    assertThat(
      new JsonApiConfiguration()
        .withJsonApiObject(new JsonApiObject(true))
        .getJsonApiObject()
        .getVersion()
    ).isEqualTo("1.1");
  }

  @Test
  void should_set_page_meta_automatically_created() {
    assertThat(
      new JsonApiConfiguration()
        .withPageMetaAutomaticallyCreated(false)
        .isPageMetaAutomaticallyCreated()
    ).isFalse();
  }

  @Test
  void should_set_type_for_class_used_for_deserialization() {
    assertThat(
      new JsonApiConfiguration()
        .withTypeForClassUsedForDeserialization(true)
        .isTypeForClassUsedForDeserialization()
    ).isTrue();
  }

  @Test
  void should_set_type_for_class() {
    assertThat(
      new JsonApiConfiguration()
        .withTypeForClass(Movie.class, "mymovies")
        .getTypeForClass(Movie.class)
    ).isEqualTo("mymovies");
  }

  @Test
  void should_customize_object_mapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(
      DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
    );
    new JsonApiConfiguration()
      .withObjectMapperCustomizer(mapper ->
        objectMapper.enable(
          DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
        )
      )
      .customize(objectMapper);
    assertThat(
      objectMapper.isEnabled(
        DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT
      )
    ).isTrue();
  }

  @Test
  void should_set_affordance_type() {
    assertThat(
      new JsonApiConfiguration()
        .withAffordancesRenderedAsLinkMeta(
          JsonApiConfiguration.AffordanceType.SPRING_HATEOAS
        )
        .getAffordancesRenderedAsLinkMeta()
    ).isEqualTo(JsonApiConfiguration.AffordanceType.SPRING_HATEOAS);
  }

  @Test
  void should_set_link_property_rendering() {
    assertThat(
      new JsonApiConfiguration()
        .withJsonApi11LinkPropertiesRemovedFromLinkMeta(false)
        .isJsonApi11LinkPropertiesRemovedFromLinkMeta()
    ).isFalse();
  }

  @Test
  void should_set_links_not_url_encoded() {
    assertThat(
      new JsonApiConfiguration()
        .withLinksNotUrlEncoded(
          Set.of(IanaLinkRelations.SELF, IanaLinkRelations.RELATED)
        )
        .getLinksNotUrlEncoded()
    ).containsExactlyInAnyOrder(
      IanaLinkRelations.SELF,
      IanaLinkRelations.RELATED
    );
  }
}
