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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;

/**
 * {@link LinkDiscoverer} implementation based on JSON:API link structure.
 *
 * @author Kai Toedter
 */
public class JsonApiLinkDiscoverer implements LinkDiscoverer {

  private final ObjectMapper mapper;

  /**
   * Constructor for {@link MediaTypes#JSON_API}.
   */
  public JsonApiLinkDiscoverer() {
    this.mapper = new ObjectMapper();
    new JsonApiMediaTypeConfiguration(null, null).configureObjectMapper(
      this.mapper,
      new JsonApiConfiguration()
    );
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
   */
  @Override
  public Optional<Link> findLinkWithRel(
    LinkRelation rel,
    String representation
  ) {
    return getLinks(representation)
      .stream() //
      .filter(it -> it.hasRel(rel)) //
      .findFirst();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.LinkDiscoverer#findLinkWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
   */
  @Override
  public Optional<Link> findLinkWithRel(
    LinkRelation rel,
    InputStream representation
  ) {
    return getLinks(representation)
      .stream() //
      .filter(it -> it.hasRel(rel)) //
      .findFirst();
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.lang.String)
   */
  @Override
  public Links findLinksWithRel(LinkRelation rel, String representation) {
    return getLinks(representation)
      .stream() //
      .filter(it -> it.hasRel(rel)) //
      .collect(Links.collector());
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.LinkDiscoverer#findLinksWithRel(org.springframework.hateoas.LinkRelation, java.io.InputStream)
   */
  @Override
  public Links findLinksWithRel(LinkRelation rel, InputStream representation) {
    return getLinks(representation)
      .stream() //
      .filter(it -> it.hasRel(rel)) //
      .collect(Links.collector());
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.plugin.core.Plugin#supports(java.lang.Object)
   */
  @Override
  public boolean supports(MediaType delimiter) {
    return delimiter.isCompatibleWith(JSON_API);
  }

  /**
   * Deserialize the entire document to find links.
   *
   * @param json the json input
   * @return the Links
   */
  private Links getLinks(String json) {
    try {
      JsonApiDocument jsonApiDocument = this.mapper.readValue(
        json,
        JsonApiDocument.class
      );
      final Links links = jsonApiDocument.getLinks();
      if (links != null) {
        return links;
      }
      return Links.NONE;
    } catch (IOException e) {
      throw new IllegalArgumentException("Cannot get links from JSON", e);
    }
  }

  /**
   * Deserialize the entire JSON:API document to find links.
   *
   * @param stream the json input as stream
   * @return the Links
   */
  private Links getLinks(InputStream stream) {
    try {
      JsonApiDocument jsonApiDocument = this.mapper.readValue(
        stream,
        JsonApiDocument.class
      );
      final Links links = jsonApiDocument.getLinks();
      if (links != null) {
        return links;
      }
      return Links.NONE;
    } catch (IOException e) {
      throw new IllegalArgumentException(
        "Cannot get links from InputStream",
        e
      );
    }
  }
}
