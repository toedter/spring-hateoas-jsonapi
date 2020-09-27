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

import com.toedter.spring.hateoas.jsonapi.support.Movie;
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
        assertThat(new JsonApiConfiguration().isPluralizedTypeRendered()).isTrue();
        assertThat(new JsonApiConfiguration().isLowerCasedTypeRendered()).isTrue();
        assertThat(new JsonApiConfiguration().isJsonApiVersionRendered()).isFalse();
        assertThat(new JsonApiConfiguration().isPageMetaAutomaticallyCreated()).isTrue();
    }

    @Test
    void should_set_pluralized_type() {
        assertThat(new JsonApiConfiguration().withPluralizedTypeRendered(false).isPluralizedTypeRendered()).isFalse();
    }

    @Test
    void should_set_lower_cased_type() {
        assertThat(new JsonApiConfiguration().withLowerCasedTypeRendered(false).isLowerCasedTypeRendered()).isFalse();
    }

    @Test
    void should_set_render_version() {
        assertThat(new JsonApiConfiguration().withJsonApiVersionRendered(true).isJsonApiVersionRendered()).isTrue();
    }

    @Test
    void should_set_page_meta_auotomatically_created() {
        assertThat(new JsonApiConfiguration().withPageMetaAutomaticallyCreated(false)
                .isPageMetaAutomaticallyCreated()).isFalse();
    }

    @Test
    void should_set_type_for_class() {
        assertThat(new JsonApiConfiguration().withTypeForClass(Movie.class, "mymovies")
                .getTypeForClass(Movie.class)).isEqualTo("mymovies");
    }
}
