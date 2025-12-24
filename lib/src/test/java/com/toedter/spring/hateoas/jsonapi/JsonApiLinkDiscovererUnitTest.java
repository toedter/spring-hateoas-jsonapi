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

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.hateoas.IanaLinkRelations.APPENDIX;
import static org.springframework.hateoas.IanaLinkRelations.SELF;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.MediaType;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiLinkDiscoverer Unit Test")
class JsonApiLinkDiscovererUnitTest extends JsonApiTestBase {

  private LinkDiscoverer discoverer = new JsonApiLinkDiscoverer();

  @BeforeEach
  void beforeEach() {
    discoverer = new JsonApiLinkDiscoverer();
  }

  @Test
  void should_only_support_jsonapi_media_type() {
    assertThat(discoverer.supports(JSON_API)).isTrue();
    assertThat(discoverer.supports(MediaType.APPLICATION_JSON)).isFalse();
  }

  @Test
  void should_throw_exception_for_misformatted_json() {
    String source = "murks";
    assertThrows(IllegalArgumentException.class, () -> discoverer.findLinksWithRel(SELF, source));
  }

  @Nested
  class FromString {

    @Test
    void should_find_self_link_as_string() throws IOException {
      String source = readFile("movieEntityModelWithLinks.json");

      Optional<Link> link = discoverer.findLinkWithRel(SELF, source);
      assertThat(link).map(Link::getHref).hasValue("http://localhost/movies/1");
    }

    @Test
    void should_find_self_link_as_object() throws IOException {
      String source = readFile("movieEntityModelWithLinksObject.json");

      Optional<Link> link = discoverer.findLinkWithRel(SELF, source);
      assertThat(link).map(Link::getHref).hasValue("http://localhost/movies/1");
    }

    @Test
    void should_return_empty_optional_if_not_available() throws IOException {
      String source = readFile("movieEntityModelWithLinksObject.json");
      assertThat(discoverer.findLinkWithRel(APPENDIX, source)).isEmpty();
    }

    @Test
    void should_find_links_as_string() throws IOException {
      String source = readFile("movieEntityModelWithLinks.json");
      // tag::link-discoverer[]
      LinkDiscoverer linkDiscoverer = new JsonApiLinkDiscoverer();
      Links links = linkDiscoverer.findLinksWithRel(SELF, source);

      assertThat(links.hasLink("self")).isTrue();
      assertThat(links).map(Link::getHref).contains("http://localhost/movies/1");
      // end::link-discoverer[]
    }

    @Test
    void should_not_find_links() throws IOException {
      String source = readFile("movieEntityModel.json");
      Links links = discoverer.findLinksWithRel(SELF, source);
      assertThat(links.hasSize(0)).isTrue();
    }
  }

  @Nested
  class FromStream {

    @Test
    void should_find_self_link_as_string_from_stream() throws IOException {
      InputStream source = getStream("movieEntityModelWithLinks.json");

      Optional<Link> link = discoverer.findLinkWithRel(SELF, source);
      assertThat(link).map(Link::getHref).hasValue("http://localhost/movies/1");
    }

    @Test
    void should_find_self_link_as_object_from_stream() throws IOException {
      InputStream source = getStream("movieEntityModelWithLinksObject.json");

      Optional<Link> link = discoverer.findLinkWithRel(SELF, source);
      assertThat(link).map(Link::getHref).hasValue("http://localhost/movies/1");
    }

    @Test
    void should_return_empty_optional_if_not_available_from_stream() throws IOException {
      InputStream source = getStream("movieEntityModelWithLinksObject.json");
      assertThat(discoverer.findLinkWithRel(APPENDIX, source)).isEmpty();
    }

    @Test
    void should_find_links_as_string_from_stream() throws IOException {
      InputStream source = getStream("movieEntityModelWithLinks.json");
      Links links = discoverer.findLinksWithRel(SELF, source);

      assertThat(links.hasLink("self")).isTrue();
      assertThat(links).map(Link::getHref).contains("http://localhost/movies/1");
    }

    @Test
    void should_not_find_links() throws IOException {
      InputStream source = getStream("movieEntityModel.json");
      Links links = discoverer.findLinksWithRel(SELF, source);
      assertThat(links.hasSize(0)).isTrue();
    }
  }
}
