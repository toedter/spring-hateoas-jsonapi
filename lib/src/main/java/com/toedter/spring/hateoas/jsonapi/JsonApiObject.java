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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * This class represents a JSON:API object compliant to the
 * see <a href="https://jsonapi.org/format/#document-jsonapi-object">JSON:API 1.1 specification</a>.
 */
@Getter(onMethod_ = { @JsonProperty })
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JsonApiObject {

  /**
   * Gets the supported JSON:API version.
   *
   * @return the supported JSON:API version ("1.1") or null
   */
  @Nullable
  private final String version;

  /**
   * Gets the list of JSON:API extensions.
   *
   * @return list of JSON:API extensions or null
   */
  @Nullable
  private final List<URI> ext;

  /**
   * Gets the list of JSON:API profiles.
   *
   * @return list of JSON:API profiles or null
   */
  @Nullable
  private final List<URI> profile;

  /**
   * Gets the JSON:API object meta.
   *
   * @return the JSON:API object meta or null
   */
  @Nullable
  private final Map<String, Object> meta;

  /**
   * Creates an empty JSON:API object for deserialization.
   */
  public JsonApiObject() {
    this.version = null;
    this.ext = null;
    this.profile = null;
    this.meta = null;
  }

  /**
   * Creates a JSON:API object compliant to the
   * see <a href="https://jsonapi.org/format/#document-jsonapi-object">JSON:API 1.1 spec</a>.
   *
   * @param showVersion true, if JSON:API version should be rendered
   * @param ext         list of JSON:API extensions
   * @param profile     list of JSON:API profiles
   * @param meta        the JSON:API object meta
   */
  public JsonApiObject(
    boolean showVersion,
    @Nullable List<URI> ext,
    @Nullable List<URI> profile,
    @Nullable Map<String, Object> meta
  ) {
    if (showVersion) {
      this.version = "1.1";
    } else {
      this.version = null;
    }

    this.ext = ext;
    this.profile = profile;
    this.meta = meta;
  }

  /**
   * Creates a JSON:API object compliant to the
   * see <a href="https://jsonapi.org/format/#document-jsonapi-object">JSON:API 1.1 spec</a>.
   *
   * @param showVersion true, if JSON:API version should be rendered
   */
  public JsonApiObject(boolean showVersion) {
    this(showVersion, null, null, null);
  }
}
