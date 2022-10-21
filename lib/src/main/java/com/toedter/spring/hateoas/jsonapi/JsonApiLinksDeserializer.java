/*
 * Copyright 2022 the original author or authors.
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
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonApiLinksDeserializer extends ContainerDeserializerBase<Links> {

    protected JsonApiLinksDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
    }

    @Override
    @Nullable
    public JsonDeserializer<Object> getContentDeserializer() {
        return null;
    }

    @Override
    public Links deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JavaType type = ctxt.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
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

    private void deserializeLink(List<Link> links, String rel, Object linkObject) {
        if (linkObject instanceof String) {
            links.add(Link.of(linkObject.toString(), rel));
        } else if (linkObject instanceof LinkedHashMap) {
            @SuppressWarnings("rawtypes")
            LinkedHashMap<?, ?> linkedHashMap = (LinkedHashMap) linkObject;
            Object href = linkedHashMap.get("href");
            Object meta = linkedHashMap.get("meta");
            if (href instanceof String) {
                Link link = Link.of(href.toString(), rel);
                if (meta instanceof LinkedHashMap) {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    LinkedHashMap<String, String> attributes = (LinkedHashMap) meta;
                    if (linkedHashMap.containsKey("hreflang")) {
                        link = link.withHreflang(linkedHashMap.get("hreflang").toString());
                    }

                    if (linkedHashMap.containsKey("title")) {
                        link = link.withTitle(linkedHashMap.get("title").toString());
                    }

                    if (linkedHashMap.containsKey("type")) {
                        link = link.withType(linkedHashMap.get("type").toString());
                    }

                    if (attributes.containsKey("media")) {
                        link = link.withMedia(attributes.get("media"));
                    }

                    if (attributes.containsKey("deprecation")) {
                        link = link.withDeprecation(attributes.get("deprecation"));
                    }

                    if (attributes.containsKey("profile")) {
                        link = link.withProfile(attributes.get("profile"));
                    }

                    if (attributes.containsKey("name")) {
                        link = link.withName(attributes.get("name"));
                    }
                }
                links.add(link);
            }
        }
    }
}
