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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiMediaTypeConfiguration Unit Test")
class JsonApiMediaTypeConfigurationUnitTest {
    private JsonApiConfiguration jsonApiConfiguration;
    private JsonApiMediaTypeConfiguration configuration;

    private static final String DEFAULT_MEDIA_TYPE = "application/vnd.api+json";

    @BeforeEach
    void setUpModule() {
        configuration = new JsonApiMediaTypeConfiguration(new JsonApiConfigurationProvider(), null);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { DEFAULT_MEDIA_TYPE, "application/vnd.test.api+json"})
    public void should_return_json_api_media_type(String mediaType) {
        String expectedType = DEFAULT_MEDIA_TYPE;
        if (mediaType != null) {
            jsonApiConfiguration = new JsonApiConfiguration().withMediaType(MediaType.parseMediaType(mediaType));
            expectedType = mediaType;
        }
        List<MediaType> mediaTypes = configuration.getMediaTypes();
        assertThat(mediaTypes.size()).isEqualTo(1);
        assertThat(mediaTypes.get(0).toString()).isEqualTo(expectedType);
    }

    @Test
    public void should_return_json_api_jackson_module() {
        Module jacksonModule = configuration.getJacksonModule();
        assertThat(jacksonModule).isInstanceOf(Jackson2JsonApiModule.class);
    }

    @Test
    public void should_return_configured_object_mapper() {
        ObjectMapper objectMapper = configuration.configureObjectMapper(new ObjectMapper(), new JsonApiConfiguration());
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)).isFalse();
        assertThat(objectMapper.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)).isFalse();
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        assertThat(objectMapper.getRegisteredModuleIds().
                contains("com.toedter.spring.hateoas.jsonapi.Jackson2JsonApiModule")).isTrue();
    }

    private class JsonApiConfigurationProvider implements ObjectProvider<JsonApiConfiguration> {

        @Override
        public JsonApiConfiguration getObject(final Object... objects) throws BeansException {
            return jsonApiConfiguration;
        }

        @Override
        public JsonApiConfiguration getIfAvailable() throws BeansException {
            return jsonApiConfiguration;
        }

        @Override
        public JsonApiConfiguration getIfUnique() throws BeansException {
            return jsonApiConfiguration;
        }

        @Override
        public JsonApiConfiguration getObject() throws BeansException {
            return jsonApiConfiguration;
        }
    }

}
