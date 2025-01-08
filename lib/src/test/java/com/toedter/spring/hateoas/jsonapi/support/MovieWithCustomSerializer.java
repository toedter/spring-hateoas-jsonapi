package com.toedter.spring.hateoas.jsonapi.support;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class MovieWithCustomSerializer {

  private String id;
  private String title;

  @JsonSerialize(using = CustomSerializer.class)
  private String customSer;

  static class CustomSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(
      String value,
      JsonGenerator gen,
      SerializerProvider serializers
    ) throws IOException {
      gen.writeString("custom: " + value);
    }
  }
}
