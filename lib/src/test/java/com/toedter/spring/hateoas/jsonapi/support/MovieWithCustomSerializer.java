package com.toedter.spring.hateoas.jsonapi.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class MovieWithCustomSerializer {

  private String id;
  private String title;

  @JsonSerialize(using = CustomSerializer.class)
  private String customSer;

  static class CustomSerializer extends ValueSerializer<String> {

    @Override
    public void serialize(
      String value,
      JsonGenerator gen,
      SerializationContext serializers
    ) {
      gen.writeString("custom: " + value);
    }
  }
}
