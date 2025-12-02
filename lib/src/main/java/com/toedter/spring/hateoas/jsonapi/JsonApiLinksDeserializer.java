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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

class JsonApiLinksDeserializer extends ContainerDeserializerBase<Links> {

  private static final String HREFLANG = "hreflang";
  private static final String TITLE = "title";
  private static final String TYPE = "type";
  private static final String MEDIA = "media";
  private static final String DEPRECATION = "deprecation";
  private static final String PROFILE = "profile";
  private static final String NAME = "name";

  protected JsonApiLinksDeserializer() {
    super(
      TypeFactory.defaultInstance().constructCollectionLikeType(
        List.class,
        Link.class
      )
    );
  }

  @Override
  @Nullable
  public JsonDeserializer<Object> getContentDeserializer() {
    return null;
  }

  @Override
  public Links deserialize(JsonParser jp, DeserializationContext ctxt)
    throws IOException {
    JavaType type = ctxt
      .getTypeFactory()
      .constructMapType(HashMap.class, String.class, Object.class);
    List<Link> links = new ArrayList<>();
    Map<String, Object> jsonApiLinks = jp.getCodec().readValue(jp, type);
    jsonApiLinks.forEach((rel, object) -> {
      if (object instanceof List) {
        for (Object linkObject : (List<?>) object) {
          deserializeLink(links, rel, linkObject);
        }
      } else {
        deserializeLink(links, rel, object);
      }
    });
    return Links.of(links);
  }

  private void deserializeLink(
    List<Link> links,
    String rel,
    Object linkObject
  ) {
    if (linkObject instanceof String) {
      links.add(Link.of(linkObject.toString(), rel));
    } else if (linkObject instanceof LinkedHashMap linkedHashMap) {
      Object href = linkedHashMap.get("href");
      Object meta = linkedHashMap.get("meta");
      if (href instanceof String) {
        Link link = Link.of(href.toString(), rel);
        if (meta instanceof LinkedHashMap linkedHashMapMeta) {
          LinkedHashMap<String, String> attributes = linkedHashMapMeta;
          link = getLink(attributes, link);

          if (attributes.containsKey(MEDIA)) {
            link = link.withMedia(attributes.get(MEDIA));
          }

          if (attributes.containsKey(DEPRECATION)) {
            link = link.withDeprecation(attributes.get(DEPRECATION));
          }

          if (attributes.containsKey(PROFILE)) {
            link = link.withProfile(attributes.get(PROFILE));
          }

          if (attributes.containsKey(NAME)) {
            link = link.withName(attributes.get(NAME));
          }
        }

        link = getLink(linkedHashMap, link);

        links.add(link);
      }
    }
  }

  private static Link getLink(LinkedHashMap<?, ?> linkedHashMap, Link link) {
    if (linkedHashMap.containsKey(HREFLANG)) {
      link = link.withHreflang(linkedHashMap.get(HREFLANG).toString());
    }

    if (linkedHashMap.containsKey(TITLE)) {
      link = link.withTitle(linkedHashMap.get(TITLE).toString());
    }

    if (linkedHashMap.containsKey(TYPE)) {
      link = link.withType(linkedHashMap.get(TYPE).toString());
    }
    return link;
  }
}
