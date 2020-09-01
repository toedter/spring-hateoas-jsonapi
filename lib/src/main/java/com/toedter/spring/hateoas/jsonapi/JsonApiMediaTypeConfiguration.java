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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Configuration
class JsonApiMediaTypeConfiguration implements HypermediaMappingInformation {

    private final ObjectProvider<JsonApiConfiguration> configuration;
    private final AutowireCapableBeanFactory beanFactory;

    @Override
    @NonNull
    public List<MediaType> getMediaTypes() {
        return Collections.singletonList(getJsonApiConfiguration().getMediaType());
    }

    @Override
    public Module getJacksonModule() {
        return new Jackson2JsonApiModule();
    }

    @Override
    @NonNull
    public ObjectMapper configureObjectMapper(@NonNull ObjectMapper mapper) {
        return this.configureObjectMapper(
                mapper, getJsonApiConfiguration());
    }

    @NonNull
    ObjectMapper configureObjectMapper(
            @NonNull ObjectMapper mapper,
            JsonApiConfiguration configuration) {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.registerModule(new Jackson2JsonApiModule());
        mapper.setHandlerInstantiator(new JsonApiHandlerInstantiator(
                configuration, beanFactory));

        return mapper;
    }

    private JsonApiConfiguration getJsonApiConfiguration() {
        return configuration.getIfAvailable(JsonApiConfiguration::new);
    }
}
