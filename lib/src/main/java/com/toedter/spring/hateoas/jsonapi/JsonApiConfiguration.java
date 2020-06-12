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

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;
import org.springframework.hateoas.mediatype.hal.HalConfiguration;

/**
 * JSON:API specific configuration.
 *
 * @author Kai Toedter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
@Getter
public class JsonApiConfiguration {

    /**
     * Configures how to render JSON:API type attributes.
     * By default, JSON:API type attributes will be rendered pluralized.
     * <p>
     * Precondition: At runtime, evo-inflector must be in the classpath
     */
    private final boolean pluralizedTypeRendered;

    /**
     * Configures how to render JSON:API document.
     * By default, the JSON:API will not be rendered.
     * <p>
     * If set to true, each rendered JSON:API document will start with
     *
     * <pre>
     * "jsonapi": {
     *     "version": "1.0"
     * }
     * </pre>
     */
    private final boolean jsonApiVersionRendered;

    /**
     * Creates a new default {@link HalConfiguration} rendering single links as immediate sub-document.
     */
    public JsonApiConfiguration() {

        this.pluralizedTypeRendered = true;
        this.jsonApiVersionRendered = false;
    }
}
