/*
 * Copyright 2023 the original author or authors.
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
/*
 * Copyright 2023 the original author or authors.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.toedter.spring.hateoas.jsonapi.support.Movie;
import com.toedter.spring.hateoas.jsonapi.support.MovieWithDirectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.server.core.TypeReferences;
import org.springframework.hateoas.server.core.TypeReferences.EntityModelType;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.MockRestServiceServer.createServer;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Integration tests for {@link TypeReferences}.
 *
 * @author Kai Tödter
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@DisplayName("JsonApi RestTemplate Integration Test")
class RestTemplateIntegrationTest {

    @Autowired
    RestTemplate template;
    MockRestServiceServer server;

    // tag::restTemplateConfig[]
    @Configuration
    @EnableHypermediaSupport(type = {})
    static class Config {
        public @Bean
        RestTemplate template() {
            return new RestTemplate();
        }

        @Bean
        public JsonApiMediaTypeConfiguration jsonApiMediaTypeConfiguration(
                ObjectProvider<JsonApiConfiguration> configuration,
                AutowireCapableBeanFactory beanFactory) {
            return new JsonApiMediaTypeConfiguration(configuration, beanFactory);
        }
    }
    // end::restTemplateConfig[]

    @BeforeEach
    void setUp() {
        this.server = createServer(template);
    }

    @Test
    void should_deserialize_entity_model_with_link() throws Exception {
        String jsonResult = createJsonStringFromFile("movieEntityModelWithLinks.json");
        server.expect(requestTo("/movie")).andRespond(withSuccess(jsonResult, MediaTypes.JSON_API));

        ResponseEntity<EntityModel<Movie>> response =
                template.exchange("/movie", HttpMethod.GET, null,
                        new EntityModelType<Movie>() {
                        });

        EntityModel<Movie> entityModel = response.getBody();
        assertThat(entityModel.getLink("self")).isPresent();

        Movie movie = entityModel.getContent();
        assertThat(movie.getId()).isEqualTo("1");
        assertThat(movie.getTitle()).isEqualTo("Star Wars");
    }

    @Test
    void should_deserialize_entity_model_with_relationship() throws Exception {
        String jsonResult = createJsonStringFromFile("movieCreatedWithSingleDirector.json");
        server.expect(requestTo("/movie")).andRespond(withSuccess(jsonResult, MediaTypes.JSON_API));

        ResponseEntity<EntityModel<MovieWithDirectors>> response =
                template.exchange("/movie", HttpMethod.GET, null,
                        new EntityModelType<MovieWithDirectors>() {
                        });

        EntityModel<MovieWithDirectors> entityModel = response.getBody();
        MovieWithDirectors movie = entityModel.getContent();
        assertThat(movie.getDirectors().get(0).getId()).isEqualTo("1");
    }

    @Test
    void should_deserialize_entity_model_with_relationships_and_included() throws Exception {
        String jsonResult = createJsonStringFromFile("movieWithIncludedRelationships.json");
        server.expect(requestTo("/movie")).andRespond(withSuccess(jsonResult, MediaTypes.JSON_API));

        ResponseEntity<EntityModel<MovieWithDirectors>> response =
                template.exchange("/movie", HttpMethod.GET, null,
                        new EntityModelType<MovieWithDirectors>() {
                        });

        EntityModel<MovieWithDirectors> entityModel = response.getBody();
        MovieWithDirectors movie = entityModel.getContent();
        assertThat(movie.getDirectors().get(0).getId()).isEqualTo("3");
        assertThat(movie.getDirectors().get(0).getName()).isEqualTo("George Lucas");
    }


    private String createJsonStringFromFile(String fileName) throws Exception {
        File file = new ClassPathResource(fileName, getClass()).getFile();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonMapper.builder().configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JsonNode jsonNode = objectMapper.readValue(file, JsonNode.class);
        return jsonNode.toString();
    }
}
