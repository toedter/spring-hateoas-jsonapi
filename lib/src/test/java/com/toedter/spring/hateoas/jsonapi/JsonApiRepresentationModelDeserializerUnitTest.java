/*
 * Copyright 2023 the original author or authors.
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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiRepresentationModelDeserializer Unit Test")
public class JsonApiRepresentationModelDeserializerUnitTest {
    private JsonApiRepresentationModelDeserializer deserializer;

    @BeforeEach
    void setUpModule() {
        deserializer = new JsonApiRepresentationModelDeserializer(new JsonApiConfiguration());
    }

    @Test
    public void should_throw_exception_with_null_arguments() {
        List<Object> list = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.convertToRepresentationModel(list, null));
    }

    @Test
    public void should_throw_exception_with_wrong_list_size() {
        List<Object> list = new ArrayList<>();
        JsonApiDocument jsonApiDocument = new JsonApiDocument();
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.convertToRepresentationModel(list, jsonApiDocument));
    }

    @Test
    public void should_throw_exception_with_wrong_list_content() {
        List<Object> list = new ArrayList<>();
        list.add(new Object());
        JsonApiDocument jsonApiDocument = new JsonApiDocument();
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.convertToRepresentationModel(list, jsonApiDocument));
    }
}
