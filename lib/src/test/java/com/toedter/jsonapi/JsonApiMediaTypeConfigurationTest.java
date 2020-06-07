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

package com.toedter.jsonapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.Jackson2JsonApiModule;
import com.toedter.spring.hateoas.jsonapi.JsonApiMediaTypeConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonApiMediaTypeConfigurationTest {
    private JsonApiMediaTypeConfiguration configuration;

    @BeforeEach
    void setUpModule() {
        configuration = new JsonApiMediaTypeConfiguration();
    }

    @Test
    public void shouldReturnJsonApiMediaType() {
        List<MediaType> mediaTypes = configuration.getMediaTypes();
        assertThat(mediaTypes.size()).isEqualTo(1);
        assertThat(mediaTypes.get(0).toString()).isEqualTo("application/vnd.api+json");
    }

    @Test
    public void shouldReturnJsonApiJacksonModule() {
        Module jacksonModule = configuration.getJacksonModule();
        assertThat(jacksonModule).isInstanceOf(Jackson2JsonApiModule.class);
    }

    @Test
    public void shouldReturnConfiguredObjectMapper() {
        ObjectMapper objectMapper = configuration.configureObjectMapper(new ObjectMapper());
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)).isTrue();
        assertThat(objectMapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)).isTrue();
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThat(objectMapper.getRegisteredModuleIds().
                contains("com.toedter.spring.hateoas.jsonapi.Jackson2JsonApiModule")).isTrue();
    }

}
