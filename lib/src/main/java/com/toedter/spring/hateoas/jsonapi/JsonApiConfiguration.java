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
import org.springframework.util.Assert;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JSON:API specific configuration.
 *
 * @author Kai Toedter
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonApiConfiguration {

    /**
     * Indicates if the JSON:API type attribute of resource objects is pluralized.
     *
     * @param pluralizedTypeRendered The new value of this configuration's pluralizedTypeRendered
     * @return The default is {@literal true}.
     */
    @With @Getter private final boolean pluralizedTypeRendered;

    /**
     * Indicates if the JSON:API type attribute of resource objects is lower-cased.
     *
     * @param lowerCasedTypeRendered The new value of this configuration's lowerCasedTypeRendered
     * @return The default is {@literal true}.
     */
    @With @Getter private final boolean lowerCasedTypeRendered;

    /**
     * Indicates if the JSON:API version is rendered.
     * <p>
     * If set to true, each rendered JSON:API document will start with
     *
     * <code>
     * "jsonapi": {
     *     "version": "1.0"
     * }
     * </code>
     *
     * @param jsonApiVersionRendered The new value of this configuration's jsonApiVersionRendered
     * @return The default is {@literal false}.
     */
    private final @With @Getter boolean jsonApiVersionRendered;

    /**
     * Indicates if page meta data (rendered as top level JSON:API meta)
     * for a paged model is created automatically.
     *
     * @param pageMetaAutomaticallyCreated The new value of this configuration's paginationLinksAutomaticallyCreated
     * @return The default is {@literal true}.
     */
    private final @With @Getter boolean pageMetaAutomaticallyCreated;

    private final @With(AccessLevel.PRIVATE) Map<Class<?>, String> typeForClass;

    /**
     * Creates a mapping for a given class to get the JSON:API resource object {@literal type}
     * when rendered.
     *
     * @param clazz must not be {@literal null}.
     * @param type must not be {@literal null}.
     * @return a clone of this object, except with this updated property
     */
    public JsonApiConfiguration withTypeForClass(Class<?> clazz, String type) {

        Assert.notNull(clazz, "class must not be null!");
        Assert.notNull(type, "type must not be null!");

        Map<Class<?>, String> map = new LinkedHashMap<>(typeForClass);
        map.put(clazz, type);

        return withTypeForClass(map);
    }

    /**
     * Returns the {@literal JSON:API resource object type}
     * for a given class, when it was added with {@link #withTypeForClass(Class, String)}.
     *
     * @param clazz must not be {@literal null}.
     * @return can return {@literal null}.
     */
    public @Nullable String getTypeForClass(Class<?> clazz) {
        Assert.notNull(clazz, "class must not be null!");
        return typeForClass.get(clazz);
    }

    /**
     * Creates a new default {@link JsonApiConfiguration}.
     */
    public JsonApiConfiguration() {
        this.pluralizedTypeRendered = true;
        this.lowerCasedTypeRendered = true;
        this.jsonApiVersionRendered = false;
        this.pageMetaAutomaticallyCreated = true;
        this.typeForClass = new LinkedHashMap<>();
    }
}
