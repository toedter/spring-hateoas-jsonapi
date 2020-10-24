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

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

class JsonApiHandlerInstantiator extends HandlerInstantiator {
    private final Map<Class<?>, Object> serializers = new HashMap<>();
    private final @Nullable
    AutowireCapableBeanFactory beanFactory;

    public JsonApiHandlerInstantiator(
            JsonApiConfiguration jsonApiConfiguration, @Nullable AutowireCapableBeanFactory beanFactory) {

        this.beanFactory = beanFactory;

        this.serializers.put(JsonApiRepresentationModelSerializer.class,
                new JsonApiRepresentationModelSerializer(jsonApiConfiguration));
        this.serializers.put(JsonApiEntityModelSerializer.class,
                new JsonApiEntityModelSerializer(jsonApiConfiguration));
        this.serializers.put(JsonApiCollectionModelSerializer.class,
                new JsonApiCollectionModelSerializer(jsonApiConfiguration));
        this.serializers.put(JsonApiPagedModelSerializer.class,
                new JsonApiPagedModelSerializer(jsonApiConfiguration));

        this.serializers.put(JsonApiRelationshipSerializer.class,
                new JsonApiRelationshipSerializer(jsonApiConfiguration));
    }

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
        return (JsonDeserializer<?>) findInstance(deserClass);
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
        return (KeyDeserializer) findInstance(keyDeserClass);
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
        return (JsonSerializer<?>) findInstance(serClass);
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
        return (TypeResolverBuilder<?>) findInstance(builderClass);
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
        return (TypeIdResolver) findInstance(resolverClass);
    }

    private Object findInstance(Class<?> type) {

        Object result = serializers.get(type);

        if (result != null) {
            return result;
        }

        if (beanFactory != null) {
            return beanFactory.createBean(type);
        }

        return BeanUtils.instantiateClass(type);
    }
}
