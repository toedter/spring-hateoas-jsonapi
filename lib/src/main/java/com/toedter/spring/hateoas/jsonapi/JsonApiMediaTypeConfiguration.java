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

import java.util.Collections;
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.http.MediaType;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Spring configuration for JSON:API support.
 *
 * @author Kai Toedter
 */
@Configuration
public class JsonApiMediaTypeConfiguration
  implements HypermediaMappingInformation {

  private final ObjectProvider<JsonApiConfiguration> configuration;
  private final AutowireCapableBeanFactory beanFactory;

  /**
   * Creates a new {@link JsonApiMediaTypeConfiguration}.
   *
   * @param configuration the {@link JsonApiConfiguration} object provider
   * @param beanFactory   the Spring bean factory
   */
  public JsonApiMediaTypeConfiguration(
    final ObjectProvider<JsonApiConfiguration> configuration,
    final AutowireCapableBeanFactory beanFactory
  ) {
    this.configuration = configuration;
    this.beanFactory = beanFactory;
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.config.HypermediaMappingInformation#getMediaTypes()
   */
  @Override
  @NonNull
  public List<MediaType> getMediaTypes() {
    return Collections.singletonList(MediaTypes.JSON_API);
  }

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.config.HypermediaMappingInformation#configureJsonMapper(tools.jackson.databind.json.JsonMapper.Builder)
   */
  @Override
  public JsonMapper.Builder configureJsonMapper(JsonMapper.Builder builder) {
    JsonApiConfiguration jsonApiConfiguration = configuration != null
      ? configuration.getIfAvailable(JsonApiConfiguration::new)
      : new JsonApiConfiguration();

    builder = builder
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .handlerInstantiator(
        new JsonApiHandlerInstantiator(jsonApiConfiguration, beanFactory)
      )
      .addModule(new Jackson2JsonApiModule(jsonApiConfiguration));

    return jsonApiConfiguration.customize(builder);
  }
}
