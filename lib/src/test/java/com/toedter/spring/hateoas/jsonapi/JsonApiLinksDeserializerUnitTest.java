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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import tools.jackson.databind.json.JsonMapper;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiLinksDeserializer Unit Test")
class JsonApiLinksDeserializerUnitTest extends JsonApiTestBase {

  private JsonMapper mapper;
  private JsonApiLinksDeserializer deserializer;

  @BeforeEach
  void setUp() {
    JsonApiConfiguration config = new JsonApiConfiguration();
    mapper = createJsonMapper(config);
    deserializer = new JsonApiLinksDeserializer();
  }

  @Test
  void should_deserialize_simple_string_link() throws Exception {
    String json =
        """
        {
          "self": "http://example.com/articles/1"
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(link.getRel().value()).isEqualTo("self");
  }

  @Test
  void should_deserialize_link_object_with_href() throws Exception {
    String json =
        """
        {
          "related": {
            "href": "http://example.com/articles/1/author"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("related");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1/author");
  }

  @Test
  void should_deserialize_link_with_hreflang() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "hreflang": "en-US"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(link.getHreflang()).isEqualTo("en-US");
  }

  @Test
  void should_deserialize_link_with_title() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "title": "Article Title"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getTitle()).isEqualTo("Article Title");
  }

  @Test
  void should_deserialize_link_with_type() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "type": "application/vnd.api+json"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getType()).isEqualTo("application/vnd.api+json");
  }

  @Test
  void should_deserialize_link_with_meta_attributes() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "meta": {
              "media": "screen",
              "deprecation": "http://example.com/deprecated",
              "profile": "http://example.com/profile",
              "name": "article-link"
            }
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(link.getMedia()).isEqualTo("screen");
    assertThat(link.getDeprecation()).isEqualTo("http://example.com/deprecated");
    assertThat(link.getProfile()).isEqualTo("http://example.com/profile");
    assertThat(link.getName()).isEqualTo("article-link");
  }

  @Test
  void should_deserialize_link_with_all_attributes() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "hreflang": "en-US",
            "title": "Article Title",
            "type": "application/json",
            "meta": {
              "media": "screen",
              "deprecation": "http://example.com/deprecated",
              "profile": "http://example.com/profile",
              "name": "article-link",
              "hreflang": "de-DE"
            }
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(link.getHreflang()).isEqualTo("en-US");
    assertThat(link.getTitle()).isEqualTo("Article Title");
    assertThat(link.getType()).isEqualTo("application/json");
    assertThat(link.getMedia()).isEqualTo("screen");
    assertThat(link.getDeprecation()).isEqualTo("http://example.com/deprecated");
    assertThat(link.getProfile()).isEqualTo("http://example.com/profile");
    assertThat(link.getName()).isEqualTo("article-link");
  }

  @Test
  void should_deserialize_link_array_with_strings() throws Exception {
    String json =
        """
        {
          "alternate": [
            "http://example.com/articles/1/en",
            "http://example.com/articles/1/de"
          ]
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(2);
    List<Link> alternateLinks = links.stream().filter(link -> link.hasRel("alternate")).toList();
    assertThat(alternateLinks).hasSize(2);
    assertThat(alternateLinks.get(0).getHref()).isEqualTo("http://example.com/articles/1/en");
    assertThat(alternateLinks.get(1).getHref()).isEqualTo("http://example.com/articles/1/de");
  }

  @Test
  void should_deserialize_link_array_with_objects() throws Exception {
    String json =
        """
        {
          "alternate": [
            {
              "href": "http://example.com/articles/1/en",
              "hreflang": "en"
            },
            {
              "href": "http://example.com/articles/1/de",
              "hreflang": "de"
            }
          ]
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(2);
    List<Link> alternateLinks = links.stream().filter(link -> link.hasRel("alternate")).toList();
    assertThat(alternateLinks).hasSize(2);
    assertThat(alternateLinks.get(0).getHref()).isEqualTo("http://example.com/articles/1/en");
    assertThat(alternateLinks.get(0).getHreflang()).isEqualTo("en");
    assertThat(alternateLinks.get(1).getHref()).isEqualTo("http://example.com/articles/1/de");
    assertThat(alternateLinks.get(1).getHreflang()).isEqualTo("de");
  }

  @Test
  void should_deserialize_multiple_links() throws Exception {
    String json =
        """
        {
          "self": "http://example.com/articles/1",
          "related": {
            "href": "http://example.com/articles/1/author"
          },
          "next": {
            "href": "http://example.com/articles/2",
            "title": "Next Article"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(3);
    assertThat(links.getRequiredLink("self").getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(links.getRequiredLink("related").getHref())
        .isEqualTo("http://example.com/articles/1/author");
    assertThat(links.getRequiredLink("next").getHref()).isEqualTo("http://example.com/articles/2");
    assertThat(links.getRequiredLink("next").getTitle()).isEqualTo("Next Article");
  }

  @Test
  void should_deserialize_using_helper_method_with_simple_link() {
    Map<String, Object> jsonApiLinks = new LinkedHashMap<>();
    jsonApiLinks.put("self", "http://example.com/articles/1");

    Links links = deserializer.deserialize(jsonApiLinks);

    assertThat(links).hasSize(1);
    assertThat(links.getRequiredLink("self").getHref()).isEqualTo("http://example.com/articles/1");
  }

  @Test
  void should_deserialize_using_helper_method_with_link_object() {
    Map<String, Object> jsonApiLinks = new LinkedHashMap<>();
    Map<String, Object> linkObject = new LinkedHashMap<>();
    linkObject.put("href", "http://example.com/articles/1");
    linkObject.put("title", "Article Title");
    jsonApiLinks.put("self", linkObject);

    Links links = deserializer.deserialize(jsonApiLinks);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHref()).isEqualTo("http://example.com/articles/1");
    assertThat(link.getTitle()).isEqualTo("Article Title");
  }

  @Test
  void should_deserialize_using_helper_method_with_link_array() {
    Map<String, Object> jsonApiLinks = new LinkedHashMap<>();
    List<String> linkArray = List.of("http://example.com/1", "http://example.com/2");
    jsonApiLinks.put("alternate", linkArray);

    Links links = deserializer.deserialize(jsonApiLinks);

    assertThat(links).hasSize(2);
    List<Link> alternateLinks = links.stream().filter(link -> link.hasRel("alternate")).toList();
    assertThat(alternateLinks).hasSize(2);
    assertThat(alternateLinks.get(0).getHref()).isEqualTo("http://example.com/1");
    assertThat(alternateLinks.get(1).getHref()).isEqualTo("http://example.com/2");
  }

  @Test
  void should_deserialize_using_helper_method_with_link_array_of_objects() {
    Map<String, Object> jsonApiLinks = new LinkedHashMap<>();
    Map<String, Object> link1 = new LinkedHashMap<>();
    link1.put("href", "http://example.com/1");
    link1.put("hreflang", "en");
    Map<String, Object> link2 = new LinkedHashMap<>();
    link2.put("href", "http://example.com/2");
    link2.put("hreflang", "de");
    jsonApiLinks.put("alternate", List.of(link1, link2));

    Links links = deserializer.deserialize(jsonApiLinks);

    assertThat(links).hasSize(2);
    List<Link> alternateLinks = links.stream().filter(link -> link.hasRel("alternate")).toList();
    assertThat(alternateLinks).hasSize(2);
    assertThat(alternateLinks.get(0).getHreflang()).isEqualTo("en");
    assertThat(alternateLinks.get(1).getHreflang()).isEqualTo("de");
  }

  @Test
  void should_handle_empty_links() throws Exception {
    String json = "{}";

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).isEmpty();
  }

  @Test
  void should_ignore_link_object_without_href() throws Exception {
    String json =
        """
        {
          "self": {
            "title": "No href here"
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).isEmpty();
  }

  @Test
  void should_deserialize_link_with_meta_hreflang_title_and_type() throws Exception {
    String json =
        """
        {
          "self": {
            "href": "http://example.com/articles/1",
            "meta": {
              "hreflang": "fr-FR",
              "title": "Meta Title",
              "type": "text/html"
            }
          }
        }
        """;

    Links links = mapper.readValue(json, Links.class);

    assertThat(links).hasSize(1);
    Link link = links.getRequiredLink("self");
    assertThat(link.getHreflang()).isEqualTo("fr-FR");
    assertThat(link.getTitle()).isEqualTo("Meta Title");
    assertThat(link.getType()).isEqualTo("text/html");
  }
}
