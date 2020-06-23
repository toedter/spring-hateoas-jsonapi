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

/**
 * JSON:API specific configuration.
 *
 * @author Kai Toedter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonApiConfiguration {

    /**
     * Indicates how to render JSON:API type attributes.
     * The can be rendered in singular or pluralized form.
     *
     * @param pluralizedTypeRendered The new value of this configuration's pluralizedTypeRendered
     * @return The default is {@literal true}.
     */
    @With @Getter private final boolean pluralizedTypeRendered;

    /**
     * Indicates how to render JSON:API document.
     * <p>
     * If set to true, each rendered JSON:API document will start with
     *
     * <code>
     * "jsonapi": {
     *     "version": "1.0"
     * }
     * </code>
     *
     * @param jsonApiVersionRendered The new value of this configuration's jsonApiVersionRendered
     * @return The default is {@literal false}.
     */
    private final @With @Getter boolean jsonApiVersionRendered;

    /**
     * Creates a new default {@link JsonApiConfiguration}.
     */
    public JsonApiConfiguration() {
        this.pluralizedTypeRendered = true;
        this.jsonApiVersionRendered = false;
    }
}
