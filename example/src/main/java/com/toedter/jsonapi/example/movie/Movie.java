package com.toedter.jsonapi.example.movie;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.toedter.jsonapi.example.director.Director;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.server.core.Relation;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@Relation(collectionRelation = "movies")
public class Movie {
    @Id @GeneratedValue @JsonIgnore
    private Long id;

    private String title;
    private long year;
    private String imdbId;
    private double rating;
    private int rank;
    private String thumb;

    @JsonIgnore
    @ManyToMany(mappedBy = "movies", fetch = FetchType.EAGER)
    List<Director> directors = new ArrayList<>();

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
        if(updatedMovie.title != null) {
            this.title = updatedMovie.title;
        }
        if(updatedMovie.thumb != null) {
            this.thumb = updatedMovie.thumb;
        }
        if(updatedMovie.imdbId != null) {
            this.imdbId = updatedMovie.imdbId;
        }
        if(updatedMovie.year != 0) {
            this.year = updatedMovie.year;
        }
        if(updatedMovie.rating != 0) {
            this.rating = updatedMovie.rating;
        }
        if(updatedMovie.rank != 0) {
            this.rank = updatedMovie.rank;
        }

    }
}
