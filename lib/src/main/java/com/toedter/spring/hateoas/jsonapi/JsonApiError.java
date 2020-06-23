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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to build a single {@literal JSON:API} compliant error.
 * This eroor can be added to {@link JsonApiErrors}.
 *
 * @author Kai Toedter
 */
@Value
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonPropertyOrder({"id", "links", "status", "code", "title", "detail", "source", "meta"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JsonApiError {
    private static final JsonApiError EMPTY = new JsonApiError();

    @Getter
    @With
    String id;

    @Getter
    Map<String, String> links;

    @Getter
    @With
    String status;

    @Getter
    @With
    String code;

    @Getter
    @With
    String title;

    @Getter
    @With
    String detail;

    /**
     * Gets the source of the error, can contain {@literal pointer} and/or {@literal parameter}
     *
     * @return can be {@literal null}.
     */
    @Getter
    Map<String, String> source;

    /**
     * Adds the given meta to the {@literal JSON:API} error meta.
     *
     * @param meta   meta data added to the error.
     * @return will never be {@literal null}.
     */
    @Getter
    @With
    Map<String, Object> meta;

    /**
     * Adds a source pointer to the error.
     *
     * @param sourcePointer a JSON Pointer [RFC6901] to the associated entity in the request document
     *                      [e.g. "/data" for a primary data object, or "/data/attributes/title"
     *                      for a specific attribute].
     * @return will never be {@literal null}.
     */
    public JsonApiError withSourcePointer(final String sourcePointer) {
        Map<String, String> source = new HashMap<>();
        if(this.source != null && this.source.get("parameter") != null) {
            source.put("parameter", this.source.get("parameter"));
        }
        source.put("pointer", sourcePointer);
        return new JsonApiError(this.id, this.links, this.status, this.code, this.title, this.detail, source, this.meta);
    }

    /**
     * Adds a source parameter to the error.
     *
     * @param sourceParameter a string indicating which URI query parameter caused the error.
     * @return will never be {@literal null}.
     */
    public JsonApiError withSourceParameter(final String sourceParameter) {
        Map<String, String> source = new HashMap<>();
        if(this.source != null && this.source.get("pointer") != null) {
            source.put("pointer", this.source.get("pointer"));
        }
        source.put("parameter", sourceParameter);
        return new JsonApiError(this.id, this.links, this.status, this.code, this.title, this.detail, source, this.meta);
    }

    /**
     * Adds an about link to the error.
     *
     * @param aboutLink the link describing this error
     * @return will never be {@literal null}.
     */
    public JsonApiError withAboutLink(final String aboutLink) {
        Map<String, String> links = new HashMap<>();
        links.put("about", aboutLink);
        return new JsonApiError(this.id, links, this.status, this.code, this.title, this.detail, this.source, this.meta);
    }

    /**
     * Creates an empty {@link JsonApiError}.
     */
    public JsonApiError() {
        this(null, null, null, null, null, null, null, null);
    }

    /**
     * @return Creates an empty {@link JsonApiError}.
     */
    public static JsonApiError create() {
        return EMPTY;
    }
}
