package com.toedter.spring.hateoas.jsonapi.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@With
public class MovieWithLastSeen {
    private String id;
    private String title;
    private Instant lastSeen;
}
