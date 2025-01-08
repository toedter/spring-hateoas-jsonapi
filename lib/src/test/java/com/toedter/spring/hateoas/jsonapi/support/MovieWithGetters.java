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

package com.toedter.spring.hateoas.jsonapi.support;

import com.toedter.spring.hateoas.jsonapi.JsonApiId;
import com.toedter.spring.hateoas.jsonapi.JsonApiMeta;
import com.toedter.spring.hateoas.jsonapi.JsonApiType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MovieWithGetters {

  private String myId;
  private String title;
  private String myType;
  private String myMeta;

  public MovieWithGetters() {}

  @JsonApiId
  public String getMyId() {
    return myId;
  }

  @JsonApiId
  public void setMyId(String myId) {
    this.myId = myId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @JsonApiType
  public String getMyType() {
    return myType;
  }

  @JsonApiType
  public void setMyType(String myType) {
    this.myType = myType;
  }

  @JsonApiMeta
  public String getMyMeta() {
    return myMeta;
  }

  @JsonApiMeta
  public void setMyMeta(String myMeta) {
    this.myMeta = myMeta;
  }
}
