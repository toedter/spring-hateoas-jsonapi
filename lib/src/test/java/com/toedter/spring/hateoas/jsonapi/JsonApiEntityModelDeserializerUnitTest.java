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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiEntityModelDeserializerUnitTest Unit Test")
public class JsonApiEntityModelDeserializerUnitTest {
    private JsonApiEntityModelDeserializer deserializer;

    @BeforeEach
    void setUpModule() {
        deserializer = new JsonApiEntityModelDeserializer(new JsonApiConfiguration());
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
    public void should_throw_exception_for_null_content() {
        List<Object> list = new ArrayList<>();
        list.add(null);
        JsonApiDocument jsonApiDocument = new JsonApiDocument();
        assertThrows(IllegalArgumentException.class,
                () -> deserializer.convertToRepresentationModel(list, jsonApiDocument));
    }

    @Test
    public void should_return_entity_model_for_empty_jsonapi_document() {
        List<Object> list = new ArrayList<>();
        Object object = new Object();
        list.add(object);
        JsonApiDocument jsonApiDocument = new JsonApiDocument();

        RepresentationModel<?> representationModel =
                deserializer.convertToRepresentationModel(list, jsonApiDocument);

        assertThat(representationModel).isInstanceOf(EntityModel.class);
        assertThat(((EntityModel<?>)representationModel).getContent()).isEqualTo(object);
    }
}
