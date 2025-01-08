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

package org.springframework.hateoas.mediatype.hal.forms;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.hateoas.mediatype.MessageResolver;
import org.springframework.lang.Nullable;

enum DefaultOnlyMessageResolver implements MessageResolver {
  INSTANCE;

  /*
   * (non-Javadoc)
   * @see org.springframework.hateoas.mediatype.MessageResolver#resolve(org.springframework.context.MessageSourceResolvable)
   */
  @Nullable
  @Override
  public String resolve(MessageSourceResolvable resolvable) {
    return resolvable.getDefaultMessage();
  }
}

public class HalFormsTemplateBuilderWrapper {

  @Nullable
  public static Object write(Object bean) {
    HalFormsTemplateBuilder builder = new HalFormsTemplateBuilder(
      new HalFormsConfiguration(),
      DefaultOnlyMessageResolver.INSTANCE
    );
    HalFormsTemplatePropertyWriter halFormsTemplatePropertyWriter =
      new HalFormsTemplatePropertyWriter(builder);
    try {
      return halFormsTemplatePropertyWriter.value(bean, null, null);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot write HAL-FORMS template.", e);
    }
  }
}
