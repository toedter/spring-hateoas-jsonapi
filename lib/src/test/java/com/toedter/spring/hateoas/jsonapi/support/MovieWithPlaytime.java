package com.toedter.spring.hateoas.jsonapi.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class MovieWithPlaytime {

  private String id;
  private String title;
  private Double playtime;
}
