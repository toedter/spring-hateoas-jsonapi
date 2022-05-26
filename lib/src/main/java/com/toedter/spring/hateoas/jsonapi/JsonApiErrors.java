/*
 * Copyright 2022 the original author or authors.
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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to build {@literal JSON:API} compliant error messages.
 *
 * @author Kai Toedter
 */
@ToString
@NoArgsConstructor
public class JsonApiErrors {
    /**
     * Gets list of {@link JsonApiError} objects.
     *
     * @return list of {@link JsonApiError} objects.
     */
    @Getter
    private final List<JsonApiError> errors = new ArrayList<>();

    /**
     * Creates {@literal JsonApiErrors} with initial {@link JsonApiError}.
     *
     * @param jsonApiError the initial {@link JsonApiError}
     */
    public JsonApiErrors(JsonApiError jsonApiError) {
        errors.add(jsonApiError);
    }

    /**
     * Adds an {@link JsonApiError} to the list.
     *
     * @param jsonApiError the {@link JsonApiError} to be added.
     * @return this {@link JsonApiErrors} object.
     */
    public JsonApiErrors withError(JsonApiError jsonApiError) {
        errors.add(jsonApiError);
        return this;
    }

    /**
     * Creates empty {@link JsonApiErrors}.
     *
     * @return A new {@link JsonApiErrors} object.
     */
    public static JsonApiErrors create() {
        return new JsonApiErrors();
    }
}
