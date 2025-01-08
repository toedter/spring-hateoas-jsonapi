package com.toedter.spring.hateoas.jsonapi.support;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class MovieWithLastSeen {

  private String id;
  private String title;
  private Instant lastSeen;
}
