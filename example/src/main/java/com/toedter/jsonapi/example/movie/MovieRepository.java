package com.toedter.jsonapi.example.movie;

import org.springframework.data.repository.PagingAndSortingRepository;

public interface MovieRepository extends PagingAndSortingRepository<Movie, Long> {
}
