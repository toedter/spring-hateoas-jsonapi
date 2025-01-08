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

import static org.springframework.util.ReflectionUtils.doWithFields;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

class ReflectionUtils {

  private ReflectionUtils() {}

  static Field[] getAllDeclaredFields(Class<?> leafClass) {
    final List<Field> fields = new ArrayList<>(32);
    doWithFields(leafClass, fields::add);
    return fields.toArray(new Field[0]);
  }
}
