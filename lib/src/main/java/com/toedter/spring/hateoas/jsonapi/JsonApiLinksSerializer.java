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

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkRelation;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.QueryParameter;
import org.springframework.hateoas.mediatype.hal.forms.HalFormsTemplateBuilderWrapper;
import org.springframework.http.HttpMethod;
import org.springframework.web.util.UriUtils;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.json.JsonMapper;

class JsonApiLinksSerializer extends AbstractJsonApiSerializer<Links> {

  private static final JsonMapper jsonMapper = JsonMapper.builder().build();
  private JsonApiConfiguration.AffordanceType affordanceType;
  private boolean removeHateoasLinkPropertiesFromMeta;
  private transient Set<LinkRelation> linksNotUrlEncoded = new HashSet<>();

  public JsonApiLinksSerializer() {
    super(Links.class);
    this.affordanceType = JsonApiConfiguration.AffordanceType.NONE;
  }

  public void setJsonApiConfiguration(JsonApiConfiguration jsonApiConfiguration) {
    this.affordanceType = jsonApiConfiguration.getAffordancesRenderedAsLinkMeta();
    this.removeHateoasLinkPropertiesFromMeta =
        jsonApiConfiguration.isJsonApi11LinkPropertiesRemovedFromLinkMeta();
    this.linksNotUrlEncoded = jsonApiConfiguration.getLinksNotUrlEncoded();
  }

  @Override
  public void serialize(Links value, JsonGenerator gen, SerializationContext provider) {
    Map<LinkRelation, List<Link>> linksMap = new LinkedHashMap<>();
    for (Link link : value) {
      linksMap.computeIfAbsent(link.getRel(), key -> new ArrayList<>()).add(link);
    }

    gen.writeStartObject();

    for (Map.Entry<LinkRelation, List<Link>> entry : linksMap.entrySet()) {
      List<Link> list = entry.getValue();
      if (list.size() == 1) {
        Link link = list.get(0);
        serializeLinkWithRelation(gen, link);
      }
      // JSON:API does not support arrays of links with same name.
      // So, every list with size != 1 is ignored
    }
    gen.writeEndObject();
  }

  private void serializeLinkWithRelation(JsonGenerator gen, Link link) {
    if (isSimpleLink(link)) {
      gen.writeName(link.getRel().value());
      gen.writeString(uriEncodeLinkHref(link));
    } else {
      gen.writeName(link.getRel().value());
      gen.writeStartObject();
      writeComplexLink(gen, link);
      gen.writeEndObject();
    }
  }

  private void writeComplexLink(JsonGenerator gen, Link link) {
    gen.writeName("href");
    gen.writeString(uriEncodeLinkHref(link));
    Map<String, Object> attributes = getAttributes(link);
    if (link.getTitle() != null) {
      gen.writeName("title");
      gen.writeString(link.getTitle());
      if (this.removeHateoasLinkPropertiesFromMeta) {
        attributes.remove("title");
      }
    }
    if (link.getType() != null) {
      gen.writeName("type");
      gen.writeString(link.getType());
      if (this.removeHateoasLinkPropertiesFromMeta) {
        attributes.remove("type");
      }
    }
    if (link.getHreflang() != null) {
      gen.writeName("hreflang");
      gen.writeString(link.getHreflang());
      if (this.removeHateoasLinkPropertiesFromMeta) {
        attributes.remove("hreflang");
      }
    }

    gen.writeName("meta");
    gen.writePOJO(attributes);
  }

  private boolean isSimpleLink(Link link) {
    return getAttributes(link).size() == 0;
  }

  private String uriEncodeLinkHref(Link link) {
    return linksNotUrlEncoded.contains(link.getRel())
        ? link.getHref()
        : UriUtils.encodeQuery(link.getHref(), StandardCharsets.UTF_8);
  }

  private Map<String, Object> getAttributes(Link link) {
    final Map<String, Object> attributeMap = jsonMapper.convertValue(link, Map.class);
    attributeMap.remove("rel");
    attributeMap.remove("href");
    attributeMap.remove("template");
    attributeMap.remove("affordances");

    if (!link.getAffordances().isEmpty()) {
      List<Object> affordanceList = new ArrayList<>();
      for (Affordance affordance : link.getAffordances()) {
        if (this.affordanceType == JsonApiConfiguration.AffordanceType.SPRING_HATEOAS) {
          JsonApiAffordanceModel affordanceModel = affordance.getAffordanceModel(JSON_API);
          if (affordanceModel != null && affordanceModel.getHttpMethod() != HttpMethod.GET) {
            String httpMethod = null;
            if (affordanceModel.getHttpMethod() != null) {
              httpMethod = affordanceModel.getHttpMethod().name();
            }
            SpringHateoasAffordance springHateoasAffordance =
                new SpringHateoasAffordance(
                    affordanceModel.getName(),
                    affordanceModel.getLink(),
                    httpMethod,
                    affordanceModel.getQueryMethodParameters(),
                    affordanceModel.getInputProperties(),
                    affordanceModel.getQueryProperties());
            affordanceList.add(springHateoasAffordance);
          }
        }

        if (this.affordanceType == JsonApiConfiguration.AffordanceType.HAL_FORMS) {
          AffordanceModel affordanceModel =
              affordance.getAffordanceModel(org.springframework.hateoas.MediaTypes.HAL_FORMS_JSON);
          if (affordanceModel != null && affordanceModel.getHttpMethod() != HttpMethod.GET) {
            Object halFormsTemplate =
                HalFormsTemplateBuilderWrapper.write(EntityModel.of(new Object()).add(link));
            if (halFormsTemplate != null) {
              attributeMap.put("hal-forms-templates", halFormsTemplate);
            }
          }
        }

        if (!affordanceList.isEmpty()) {
          attributeMap.put("affordances", affordanceList);
        }
      }
    }

    if (link.isTemplated()) {
      attributeMap.put("isTemplated", true);
    }
    return attributeMap;
  }

  @RequiredArgsConstructor
  @Getter
  @JsonInclude(NON_EMPTY)
  static class SpringHateoasAffordance {

    private final String name;
    private final Link link;
    private final String httpMethod;
    private final List<QueryParameter> queryMethodParameters;
    private final List<JsonApiAffordanceModel.PropertyData> inputProperties;
    private final List<JsonApiAffordanceModel.PropertyData> queryProperties;
  }
}
