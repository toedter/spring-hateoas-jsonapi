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

package com.toedter.spring.hateoas.jsonapi.example;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.toedter.spring.hateoas.jsonapi.JsonApiConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MoviesDemoApplication implements WebMvcConfigurer {
    public static void main(String... args) {
        // when deployed as a docker container to Heroku
        // Heroku sets the PORT environment variable
        // The DYNO environment variable is just to make sure to run in an Heroku environment
        String herokuPort = System.getenv().get("PORT");
        String herokuDyno = System.getenv().get("DYNO");
        if (herokuPort != null && herokuDyno != null) {
            System.getProperties().put("server.port", herokuPort);
        }

        SpringApplication.run(MoviesDemoApplication.class, args);
    }

    @Bean
    public JsonApiConfiguration jsonApiConfiguration() {
        return new JsonApiConfiguration()
                .withJsonApiVersionRendered(true)
                .withObjectMapperCustomizer(objectMapper -> {
                    // put your additional object mapper config here
                    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
                });
    }
}
