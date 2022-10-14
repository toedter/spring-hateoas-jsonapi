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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import lombok.With;
import net.minidev.json.annotate.JsonIgnore;
import org.springframework.hateoas.AffordanceModel;
import org.springframework.hateoas.mediatype.ConfiguredAffordance;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper=true)
class JsonApiAffordanceModel extends AffordanceModel {

    @Value
    @With(AccessLevel.PACKAGE)
    @Getter
    @AllArgsConstructor
    static class PropertyData {
        @With
        @Nullable
        String name;

        @With
        @Nullable
        String type;

        @With
        @JsonInclude(JsonInclude.Include.NON_DEFAULT)
        boolean required;

        PropertyData() {
            name = null;
            type = null;
            required = false;
        }
    }

    private static final Set<HttpMethod> ENTITY_ALTERING_METHODS =
            Set.of(HttpMethod.POST, HttpMethod.PUT, HttpMethod.PATCH);

    private final List<PropertyData> inputProperties;
    private final List<PropertyData> queryProperties;


    JsonApiAffordanceModel(ConfiguredAffordance configured) {

        super(configured.getNameOrDefault(), configured.getTarget(),
                configured.getMethod(), configured.getInputMetadata(), configured.getQueryParameters(),
                configured.getOutputMetadata());

        this.inputProperties = determineAffordanceInputs();
        this.queryProperties = determineQueryProperties();
    }

    private List<PropertyData> determineAffordanceInputs() {

        if (!ENTITY_ALTERING_METHODS.contains(getHttpMethod())) {
            return Collections.emptyList();
        }

        return getInput().stream()

                .map(propertyMetadata -> new PropertyData()
                        .withName(propertyMetadata.getName())
                        .withType(propertyMetadata.getInputType())
                        .withRequired(propertyMetadata.isRequired()))
                .collect(Collectors.toList());
    }

    /**
     * Transform GET-based query parameters (e.g. {@literal &query}) into a list of {@link PropertyData} objects.
     */
    private List<PropertyData> determineQueryProperties() {

        if (!getHttpMethod().equals(HttpMethod.GET)) {
            return Collections.emptyList();
        }

        if (getHttpMethod().equals(HttpMethod.GET)) {
            return getQueryMethodParameters().stream()
                    .map(queryParameter -> new PropertyData().withName(queryParameter.getName()).withType(""))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    public List<PropertyData> getInputProperties() {
        return this.inputProperties;
    }

    public List<PropertyData> getQueryProperties() {
        return this.queryProperties;
    }

    @Override
    @JsonIgnore
    public InputPayloadMetadata getInput() {
        return super.getInput();
    }
}
