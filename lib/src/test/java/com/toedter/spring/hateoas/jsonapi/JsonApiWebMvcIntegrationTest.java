/*
 * Copyright 2020 the original author or authors.
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
package com.toedter.spring.hateoas.jsonapi;


import com.toedter.spring.hateoas.jsonapi.support.WebMvcMovieController;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static com.toedter.spring.hateoas.jsonapi.MediaTypes.JSON_API;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * @author Kai Toedter
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApi WebMvc Integration Test")
class JsonApiWebMvcIntegrationTest extends AbstractJsonApiTest {
    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        this.mockMvc = webAppContextSetup(this.context).build();
        WebMvcMovieController.reset();
    }

    @Test
    void should_get_single_movie() throws Exception {
        String movieJson = this.mockMvc
                .perform(get("/movies/1").accept(JSON_API))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        compareWithFile(movieJson, "movieEntityModelWithLinks.json");
    }

    @Test
    void should_get_collection_of_movies() throws Exception {

        String moviesJson = this.mockMvc
                .perform(get("/movies").accept(JSON_API))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        compareWithFile(moviesJson, "moviesCollectionModel.json");
    }

    @Test
    void should_create_new_movie() throws Exception {

        String input = readFile("postMovie.json");

        this.mockMvc.perform(post("/movies")
                .content(input)
                .contentType(JSON_API))
                .andExpect(status().isCreated())
                .andExpect(header().stringValues(HttpHeaders.LOCATION, "http://localhost/movies/3"));

        String movieJson = this.mockMvc.perform(get("/movies/3")
                .accept(JSON_API))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        compareWithFile(movieJson, "movieCreated.json");
    }

    @Configuration
    @WebAppConfiguration
    @EnableWebMvc
    @EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL_FORMS)
    static class TestConfig {
        @Bean
        WebMvcMovieController movieController() {
            return new WebMvcMovieController();
        }
    }
}
