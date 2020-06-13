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
@AllArgsConstructor
public class JsonApiConfiguration {

    /**
     * Indicates how to render JSON:API type attributes.
     * The default is {@literal true}.
     * <p>
     * Precondition: At runtime, evo-inflector must be in the classpath
     */
    @With @Getter private final boolean pluralizedTypeRendered;

    /**
     * Indicates how to render JSON:API document.
     * The default is {@literal false}.
     * <p>
     * If set to true, each rendered JSON:API document will start with
     *
     * <pre>
     * "jsonapi": {
     *     "version": "1.0"
     * }
     * </pre>
     */
    private final @With @Getter boolean jsonApiVersionRendered;

    /**
     * Indicates if pagination links for a paged model are created automatically.
     * The default is {@literal true}.
     */
    private final @With @Getter boolean paginationLinksAutomaticallyCreated;

    /**
     * The request parameter used to indicate page number in pagination links.
     * The default is {@literal page[number]}.
     */
    private final @With @Getter String pageNumberRequestParameter;

    /**
     * The request parameter used to indicate page number in pagination links.
     * The default is {@literal page[size]}.
     */
    private final @With @Getter String pageSizeRequestParameter;

    /**
     * Creates a new default {@link JsonApiConfiguration}.
     */
    public JsonApiConfiguration() {
        this.pluralizedTypeRendered = true;
        this.jsonApiVersionRendered = false;
        this.paginationLinksAutomaticallyCreated = true;
        this.pageNumberRequestParameter = "page[number]";
        this.pageSizeRequestParameter = "page[size]";
    }
}
