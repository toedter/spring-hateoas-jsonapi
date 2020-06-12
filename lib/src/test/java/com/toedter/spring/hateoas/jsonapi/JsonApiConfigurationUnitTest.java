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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiConfiguration Unit Test")
class JsonApiConfigurationUnitTest {
    @Test
    void should_initialize_defaults() {
        assertThat(new JsonApiConfiguration().isUsePluralizedType()).isTrue();
        assertThat(new JsonApiConfiguration().isRenderJsonApiVersion()).isFalse();
    }

    @Test
    void should_set_pluralized_type() {
        new JsonApiConfiguration().withUsePluralizedType(false);
        assertThat(new JsonApiConfiguration().withUsePluralizedType(false).isUsePluralizedType()).isFalse();
    }

    @Test
    void should_set_render_version() {
        new JsonApiConfiguration().withUsePluralizedType(false);
        assertThat(new JsonApiConfiguration().withRenderJsonApiVersion(true).isRenderJsonApiVersion()).isTrue();
    }
}
