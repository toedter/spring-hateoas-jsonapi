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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiRelationshipn Unit Test")
class JsonApiRelationshipUnitTest {
    @Test
    void should_add_data_to_empty_relation() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("1", "movies"));
        JsonApiResource data = (JsonApiResource) jsonApiRelationship.getData();

        assertThat(data.getId()).isEqualTo("1");
        assertThat(data.getType()).isEqualTo("movies");
    }

    @Test
    void should_create_of_entity_model() {
        class Test {
            private String id = "1";
        }

        JsonApiRelationship jsonApiRelationship = JsonApiRelationship.of(EntityModel.of(new Test()));
        JsonApiResource data = (JsonApiResource) jsonApiRelationship.getData();

        assertThat(data.getId()).isEqualTo("1");
        assertThat(data.getType()).isEqualTo("tests");
    }

    @Test
    void should_create_of_object() {
        class Test {
            private String id = "1";
        }

        JsonApiRelationship jsonApiRelationship = JsonApiRelationship.of(new Test());
        JsonApiResource data = (JsonApiResource) jsonApiRelationship.getData();

        assertThat(data.getId()).isEqualTo("1");
        assertThat(data.getType()).isEqualTo("tests");
    }

    @Test
    void should_create_of_object_as_collection() {
        class Test {
            private String id = "1";
        }

        JsonApiRelationship jsonApiRelationship = JsonApiRelationship.of(new Test(), true);
        List<JsonApiResource> data = (List<JsonApiResource>) jsonApiRelationship.getData();

        assertThat(data.get(0).getId()).isEqualTo("1");
        assertThat(data.get(0).getType()).isEqualTo("tests");
    }

    @Test
    void should_create_of_links() {
        JsonApiRelationship jsonApiRelationship = JsonApiRelationship.of(Links.NONE);
        final Links links = jsonApiRelationship.getLinks();
        assertThat(links).isEqualTo(Links.NONE);
    }

    @Test
    void should_create_of_meta() {
        Map<String, Object> meta = new HashMap<>();
        meta.put("key", "value");

        JsonApiRelationship jsonApiRelationship = JsonApiRelationship.of(meta);

        assertThat(jsonApiRelationship.getMeta()).isEqualTo(meta);
    }

    @Test
    void should_create_with_data_as_collection() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("1", "tests"), true);

        List<JsonApiResource> data = (List<JsonApiResource>) jsonApiRelationship.getData();

        assertThat(data.get(0).getId()).isEqualTo("1");
        assertThat(data.get(0).getType()).isEqualTo("tests");
    }

    @Test
    void should_validate_relationship_with_constructor() {
        class Test {
            String id = "1";
        }

        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(new Test(), Links.of(Link.of("x")), new HashMap<>());
        assertThat(jsonApiRelationship.isValid()).isTrue();
    }

    @Test
    void should_validate_relationship_with_wither() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("1", "type"));
        jsonApiRelationship = jsonApiRelationship.withLinks(Links.of(Link.of("x")));
        jsonApiRelationship = jsonApiRelationship.withMeta(new HashMap<>());
        assertThat(jsonApiRelationship.isValid()).isTrue();
    }

    @Test
    void should_validate_relationship_with_wither_and_multiple_data() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("1", "type"));
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("2", "type"));
        jsonApiRelationship = jsonApiRelationship.addData(new JsonApiResource("3", "type"));
        jsonApiRelationship = jsonApiRelationship.withLinks(Links.of(Link.of("x")));
        jsonApiRelationship = jsonApiRelationship.withMeta(new HashMap<>());
        assertThat(jsonApiRelationship.isValid()).isTrue();
    }

    @Test
    void should_validate_invalid_null_relationship() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, null);
        assertThat(jsonApiRelationship.isValid()).isFalse();
    }

    @Test
    void should_validate_invalid_single_data() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(new Object(), null, null);
        assertThat(jsonApiRelationship.isValid()).isFalse();
    }

    @Test
    void should_validate_invalid_collection_data() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(Collections.singletonList(new Object()), null, null);
        assertThat(jsonApiRelationship.isValid()).isFalse();
    }

    @Test
    void should_validate_invalid_meta() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, null, new HashMap<>());
        assertThat(jsonApiRelationship.isValid()).isTrue();
    }

    @Test
    void should_validate_invalid_links() {
        JsonApiRelationship jsonApiRelationship = new JsonApiRelationship(null, Links.NONE, null);
        assertThat(jsonApiRelationship.isValid()).isFalse();
    }
}
