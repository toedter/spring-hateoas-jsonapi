/*
 * Copyright 2020 the original author or authors.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JsonApiLinksDeserializer extends ContainerDeserializerBase<Links> {

    protected JsonApiLinksDeserializer() {
        super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
    }

    @Override
    public JsonDeserializer<Object> getContentDeserializer() {
        return null;
    }

    @Override
    public Links deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JavaType type = ctxt.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
        List<Link> links = new ArrayList<>();
        Map<String, String> jsonApiLinks = jp.getCodec().readValue(jp, type);
        jsonApiLinks.forEach((rel, href) -> links.add(Link.of(href, rel)));
        return Links.of(links);
    }
}
