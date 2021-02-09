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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class JsonApiLinksSerializer extends AbstractJsonApiSerializer<Links> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JsonApiLinksSerializer() {
        super(Links.class);
    }

    @Override
    public void serialize(Links value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Map<LinkRelation, List<Link>> linksMap = new LinkedHashMap<>();
        for (Link link : value) {
            linksMap.computeIfAbsent(
                    link.getRel(), key -> new ArrayList<>())
                    .add(link);
        }

        gen.writeStartObject();

        for (Map.Entry<LinkRelation, List<Link>> entry : linksMap.entrySet()) {
            LinkRelation rel = entry.getKey();
            List<Link> list = entry.getValue();
            if (list.size() == 1) {
                Link link = list.get(0);
                serializeLinkWithRelation(gen, link);
            } else {
                gen.writeArrayFieldStart(rel.value());
                for(Link link: list) {
                    serializeLinkWithoutRelation(gen, link);
                }
                gen.writeEndArray();
            }
        }
        gen.writeEndObject();
    }

    private void serializeLinkWithRelation(JsonGenerator gen, Link link) throws IOException {
        if (isSimpleLink(link)) {
            gen.writeStringField(link.getRel().value(), link.getHref());
        } else {
            gen.writeObjectFieldStart(link.getRel().value());
            gen.writeStringField("href", link.getHref());
            gen.writeObjectField("meta", getAttributes(link));
            gen.writeEndObject();
        }
    }

    private void serializeLinkWithoutRelation(JsonGenerator gen, Link link) throws IOException {
        if (isSimpleLink(link)) {
            gen.writeString(link.getHref());
        } else {
            gen.writeStartObject();
            gen.writeStringField("href", link.getHref());
            gen.writeObjectField("meta", getAttributes(link));
            gen.writeEndObject();
        }
    }

    private boolean isSimpleLink(Link link) {
        return "self".equals(link.getRel().value()) || getAttributes(link).size() == 0;
    }


    private Map<String, Object> getAttributes(Link link) {
        @SuppressWarnings("unchecked") final Map<String, Object> attributeMap = objectMapper.convertValue(link, Map.class);
        attributeMap.remove("rel");
        attributeMap.remove("href");
        attributeMap.remove("template");
        attributeMap.remove("affordances");
        if (link.isTemplated()) {
            attributeMap.put("isTemplated", true);
        }
        return attributeMap;
    }
}
