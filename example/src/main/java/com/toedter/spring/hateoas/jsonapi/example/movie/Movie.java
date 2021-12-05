/*
 * Copyright 2021 the original author or authors.
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

package com.toedter.spring.hateoas.jsonapi.example.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.spring.hateoas.jsonapi.JsonApiRelationships;
import com.toedter.spring.hateoas.jsonapi.example.director.Director;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Relation(collectionRelation = "movies")
public class Movie {
    @Id
    @GeneratedValue
    @JsonIgnore
    private Long id;

    @NotNull
    private String title;
    private long year;
    private String imdbId;
    private double rating;
    private int rank;
    @JsonIgnore
    private String thumb;

    @ManyToMany(mappedBy = "movies", fetch = FetchType.EAGER)
    @JsonIgnore
    @JsonApiRelationships("directors")  // Only used for deserialization, e.g. useful when doing a HTTP POST
    private List<Director> directors = new ArrayList<>();

    public Movie(String imdbId, String title, long year, double rating, int rank, String thumb) {
        this.imdbId = imdbId;
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.rank = rank;
        this.thumb = thumb;
    }

    @Override
    public String toString() {
        return "Movie: " + this.title;
    }

    public void addDirector(Director director) {
        directors.add(director);
    }

    public void update(Movie updatedMovie) {
        if (updatedMovie.title != null) {
            this.title = updatedMovie.title;
        }
        if (updatedMovie.thumb != null) {
            this.thumb = updatedMovie.thumb;
        }
        if (updatedMovie.imdbId != null) {
            this.imdbId = updatedMovie.imdbId;
        }
        if (updatedMovie.year != 0) {
            this.year = updatedMovie.year;
        }
        if (updatedMovie.rating != 0) {
            this.rating = updatedMovie.rating;
        }
        if (updatedMovie.rank != 0) {
            this.rank = updatedMovie.rank;
        }
    }
}
