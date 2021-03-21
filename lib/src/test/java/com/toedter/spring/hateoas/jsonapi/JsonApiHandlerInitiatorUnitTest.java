/*
 * Copyright 2021 the original author or authors.
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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApiHandlerInitiator Unit Test")
class JsonApiHandlerInitiatorUnitTest {
    private JsonApiHandlerInstantiator jsonApiHandlerInstantiator;

    static class TestKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) throws IOException {
            return null;
        }

        public TestKeyDeserializer() {
        }
    }

    static class TestTypeIdResolver extends TypeIdResolverBase {
        @Override
        public String idFromValue(Object value) {
            return null;
        }

        @Override
        public String idFromValueAndType(Object value, Class<?> suggestedType) {
            return null;
        }

        @Override
        public JsonTypeInfo.Id getMechanism() {
            return null;
        }

        public TestTypeIdResolver() {
        }
    }

    static class TestTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
        public TestTypeResolverBuilder() {
            super(ObjectMapper.DefaultTyping.NON_FINAL, LaissezFaireSubTypeValidator.instance);
        }
    }

    @BeforeEach
    void beforeEach() {
        jsonApiHandlerInstantiator = new JsonApiHandlerInstantiator(new JsonApiConfiguration(), null);
    }

    @Test
    void should_get_deserialzer_instance() {
        JsonDeserializer<?> jsonDeserializer =
                jsonApiHandlerInstantiator.deserializerInstance(null, null, JsonApiEntityModelDeserializer.class);
        assertThat(jsonDeserializer).isInstanceOf(JsonApiEntityModelDeserializer.class);
    }

    @Test
    void should_get_serialzer_instance() {
        JsonSerializer<?> jsonSerializer =
                jsonApiHandlerInstantiator.serializerInstance(null, null, JsonApiEntityModelSerializer.class);
        assertThat(jsonSerializer).isInstanceOf(JsonApiEntityModelSerializer.class);
    }

    @Test
    void should_get_key_deserialzer_instance() {
        KeyDeserializer keyDeserializer =
                jsonApiHandlerInstantiator.keyDeserializerInstance(null, null, TestKeyDeserializer.class);
        assertThat(keyDeserializer).isInstanceOf(TestKeyDeserializer.class);
    }

    @Test
    void should_get_type_resolver_instance() {
        TypeIdResolver typeIdResolver =
                jsonApiHandlerInstantiator.typeIdResolverInstance(null, null, TestTypeIdResolver.class);
        assertThat(typeIdResolver).isInstanceOf(TestTypeIdResolver.class);
    }

    @Test
    void should_get_type_resolver_instance_with_bean_factory() {
        jsonApiHandlerInstantiator =
                new JsonApiHandlerInstantiator(new JsonApiConfiguration(), new DefaultListableBeanFactory());
        TypeIdResolver typeIdResolver =
                jsonApiHandlerInstantiator.typeIdResolverInstance(null, null, TestTypeIdResolver.class);
        assertThat(typeIdResolver).isInstanceOf(TestTypeIdResolver.class);
    }

    @Test
    void should_get_type_resolver_builder_instance() {
        TypeResolverBuilder<?> typeResolverBuilder =
                jsonApiHandlerInstantiator.typeResolverBuilderInstance(null, null, TestTypeResolverBuilder.class);
        assertThat(typeResolverBuilder).isInstanceOf(TestTypeResolverBuilder.class);
    }
}
