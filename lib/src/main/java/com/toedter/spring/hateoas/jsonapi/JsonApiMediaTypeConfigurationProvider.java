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


import org.springframework.hateoas.config.HypermediaMappingInformation;
import org.springframework.hateoas.config.MediaTypeConfigurationProvider;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;

import java.util.Collection;

class JsonApiMediaTypeConfigurationProvider
  implements MediaTypeConfigurationProvider {

  @Override
  @NonNull
  public Class<? extends HypermediaMappingInformation> getConfiguration() {
    return JsonApiMediaTypeConfiguration.class;
  }

  @Override
  public boolean supportsAny(@NonNull Collection<MediaType> mediaTypes) {
    // TODO: "return mediaTypes.contains(MediaTypes.JSON_API);" is not working
    return true;
  }
}