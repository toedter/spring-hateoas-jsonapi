package com.toedter.jsonapi.example.director;

import com.toedter.jsonapi.example.movie.Movie;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @Id
    @GeneratedValue
    private Long id;
    private String name;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Movie> movies = new ArrayList<>();

    public Director(String name) {
        this.name = name;
    }

    public void addMovie(Movie movie) {
        this.movies.add(movie);
    }

    public void deleteMovie(Movie movie) {
        this.movies.remove(movie);
    }
}
