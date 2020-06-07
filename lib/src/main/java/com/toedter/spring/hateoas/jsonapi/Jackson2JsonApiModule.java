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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.hateoas.*;
import org.springframework.hateoas.mediatype.JacksonHelper;
import org.springframework.hateoas.mediatype.PropertyUtils;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Jackson2JsonApiModule extends SimpleModule {

    public static final String PAGE_NUMBER = "page-number";
    public static final String PAGE_SIZE = "page-size";
    public static final String PAGE_TOTAL_ELEMENTS = "page-total-elements";
    public static final String PAGE_TOTAL_PAGES = "page-total-pages";

    public Jackson2JsonApiModule() {

        super("json-api-module",
                new Version(1, 0, 0, null,
                        "com.toedter",
                        "jsonapi-spring-hateoas"));

        addSerializer(new JsonApiEntityModelSerializer());
        addSerializer(new JsonApiRepresentationModelSerializer());
        addSerializer(new JsonApiCollectionModelSerializer());
        addSerializer(new JsonApiPagedModelSerializer());
        addSerializer(new JsonApiLinksSerializer());

        addDeserializer(Links.class, new JsonApiLinksDeserializer());

        setMixInAnnotation(EntityModel.class, EntityModelMixin.class);
        setMixInAnnotation(RepresentationModel.class, RepresentationModelMixin.class);
        setMixInAnnotation(CollectionModel.class, CollectionModelMixin.class);
        setMixInAnnotation(PagedModel.class, PagedModelMixin.class);
    }

    @JsonDeserialize(using = JsonApiEntityModelDeserializer.class)
    abstract static class EntityModelMixin<T> extends EntityModel<T> {
    }

    @JsonDeserialize(using = JsonApiRepresentationModelDeserializer.class)
    abstract static class RepresentationModelMixin extends RepresentationModel<RepresentationModelMixin> {
    }

    @JsonDeserialize(using = JsonApiCollectionModelDeserializer.class)
    abstract static class CollectionModelMixin<T> extends CollectionModel<T> {
    }

    @JsonDeserialize(using = JsonApiPagedModelDeserializer.class)
    abstract static class PagedModelMixin<T> extends PagedModel<T> {
    }

    static abstract class AbstractJsonApiSerializer<T> extends ContainerSerializer<T> {

        protected AbstractJsonApiSerializer(Class<T> t) {
            super(t);
        }

        protected AbstractJsonApiSerializer(Class<?> t, boolean dummy) {
            super(t, dummy);
        }

        @Override
        public JavaType getContentType() {
            return null;
        }

        @Override
        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        @Override
        public boolean hasSingleElement(T value) {
            return false;
        }

        @Override
        protected ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer vts) {
            return null;
        }
    }

    static class JsonApiEntityModelSerializer extends AbstractJsonApiSerializer<EntityModel<?>> {
        public JsonApiEntityModelSerializer() {
            super(EntityModel.class, false);
        }

        @Override
        public void serialize(EntityModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiRepresentationModelSerializer extends AbstractJsonApiSerializer<RepresentationModel<?>> {
        public JsonApiRepresentationModelSerializer() {
            super(RepresentationModel.class, false);
        }

        @Override
        public void serialize(RepresentationModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiCollectionModelSerializer extends AbstractJsonApiSerializer<CollectionModel<?>> {
        public JsonApiCollectionModelSerializer() {
            super(CollectionModel.class, false);
        }

        @Override
        public void serialize(CollectionModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiPagedModelSerializer extends AbstractJsonApiSerializer<PagedModel<?>> {
        public JsonApiPagedModelSerializer() {
            super(PagedModel.class, false);
        }

        @Override
        public void serialize(PagedModel<?> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            JsonApiDocument doc = new JsonApiDocument()
                    .withJsonapi(new JsonApiJsonApi())
                    .withData(JsonApiData.extractCollectionContent(value))
                    .withLinks(value.getLinks());

            if (value.getMetadata() != null) {
                Map<String, Object> metaMap = new HashMap<>();
                metaMap.put(PAGE_NUMBER, value.getMetadata().getNumber());
                metaMap.put(PAGE_SIZE, value.getMetadata().getSize());
                metaMap.put(PAGE_TOTAL_ELEMENTS, value.getMetadata().getTotalElements());
                metaMap.put(PAGE_TOTAL_PAGES, value.getMetadata().getTotalPages());
                doc = doc.withMeta(metaMap);
            }

            provider
                    .findValueSerializer(JsonApiDocument.class)
                    .serialize(doc, gen, provider);
        }
    }

    static class JsonApiLinksSerializer extends AbstractJsonApiSerializer<Links> {
        public JsonApiLinksSerializer() {
            super(Links.class);
        }

        @Override
        public void serialize(Links value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            for (Link link : value) {
                gen.writeStringField(link.getRel().value(), link.getHref());
            }
            gen.writeEndObject();
        }
    }

    static abstract class AbstractJsonApiModelDeserializer<T> extends ContainerDeserializerBase<T>
            implements ContextualDeserializer {

        protected final JavaType contentType;

        AbstractJsonApiModelDeserializer() {
            this(TypeFactory.defaultInstance().constructSimpleType(JsonApiDocument.class, new JavaType[0]));
        }

        protected AbstractJsonApiModelDeserializer(JavaType contentType) {
            super(contentType);
            this.contentType = contentType;
        }

        @Override
        public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonApiDocument doc = p.getCodec().readValue(p, JsonApiDocument.class);
            List<Object> resources = doc.getData().stream()
                    .map(this::convertToResource)
                    .collect(Collectors.toList());
            return convertToRepresentationModel(resources, doc);
        }

        protected Object convertToResource(JsonApiData jsonApiData) {
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) jsonApiData.getAttributes();
            attributes.put("id", jsonApiData.getId());
            attributes.put("_type", jsonApiData.getType());
            JavaType rootType = JacksonHelper.findRootType(this.contentType);
            return PropertyUtils.createObjectFromProperties(rootType.getRawClass(), attributes);
        }

        abstract protected T convertToRepresentationModel(List<Object> resources, JsonApiDocument doc);

        @Override
        public JavaType getContentType() {
            return this.contentType;
        }

        @Override
        @Nullable
        public JsonDeserializer<Object> getContentDeserializer() {
            return null;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property)
                throws JsonMappingException {

            JavaType type = property == null ? ctxt.getContextualType() : property.getType().getContentType();

            return createJsonDeserializer(type);
        }

        abstract protected JsonDeserializer<?> createJsonDeserializer(JavaType type);
    }

    static class JsonApiRepresentationModelDeserializer extends AbstractJsonApiModelDeserializer<RepresentationModel<?>>
            implements ContextualDeserializer {

        JsonApiRepresentationModelDeserializer() {
            super();
        }

        protected JsonApiRepresentationModelDeserializer(JavaType contentType) {
            super(contentType);
        }

        @Override
        protected RepresentationModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
            Links links = doc.getLinks();
            if (resources.size() == 1 && resources.get(0) instanceof RepresentationModel<?>) {
                RepresentationModel<?> representationModel = (RepresentationModel<?>) resources.get(0);
                representationModel.add(links);
                return representationModel;
            }
            throw new RuntimeException("Cannot deserialize input to RepresentationModel");
        }

        protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
            return new JsonApiRepresentationModelDeserializer(type);
        }
    }

    static class JsonApiEntityModelDeserializer extends AbstractJsonApiModelDeserializer<EntityModel<?>>
            implements ContextualDeserializer {

        JsonApiEntityModelDeserializer() {
            super();
        }

        protected JsonApiEntityModelDeserializer(JavaType contentType) {
            super(contentType);
        }

        @Override
        protected EntityModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
            Links links = doc.getLinks();
            if (resources.size() == 1) {
                EntityModel<Object> entityModel = EntityModel.of(resources.get(0));
                if(links != null) {
                    entityModel.add(links);
                }
                return entityModel;
            }
            throw new RuntimeException("Cannot deserialize input to EntityModel");
        }

        protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
            return new JsonApiEntityModelDeserializer(type);
        }
    }

    static class JsonApiCollectionModelDeserializer extends AbstractJsonApiModelDeserializer<CollectionModel<?>>
            implements ContextualDeserializer {

        JsonApiCollectionModelDeserializer() {
            super();
        }

        protected JsonApiCollectionModelDeserializer(JavaType contentType) {
            super(contentType);
        }

        @Override
        protected CollectionModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
            Links links = doc.getLinks();
            return CollectionModel.of(resources, links);
        }

        protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
            return new JsonApiCollectionModelDeserializer(type);
        }
    }

    static class JsonApiPagedModelDeserializer extends AbstractJsonApiModelDeserializer<PagedModel<?>>
            implements ContextualDeserializer {

        JsonApiPagedModelDeserializer() {
            super();
        }

        protected JsonApiPagedModelDeserializer(JavaType contentType) {
            super(contentType);
        }

        @Override
        protected PagedModel<?> convertToRepresentationModel(List<Object> resources, JsonApiDocument doc) {
            Links links = doc.getLinks();

            Map<String, Object> metaMap = doc.getMeta();
            long size = new Long(metaMap.get(PAGE_SIZE).toString());
            long number = new Long(metaMap.get(PAGE_NUMBER).toString());
            long totalElements = new Long(metaMap.get(PAGE_TOTAL_ELEMENTS).toString());
            long totalPages = new Long(metaMap.get(PAGE_TOTAL_PAGES).toString());

            PagedModel.PageMetadata pageMetadata = new PagedModel.PageMetadata(size, number, totalElements, totalPages);

            return PagedModel.of(resources, pageMetadata, links);
        }

        protected JsonDeserializer<?> createJsonDeserializer(JavaType type) {
            return new JsonApiPagedModelDeserializer(type);
        }
    }

    static class JsonApiLinksDeserializer extends ContainerDeserializerBase<Links> {

        protected JsonApiLinksDeserializer() {
            super(TypeFactory.defaultInstance().constructCollectionLikeType(List.class, Link.class));
        }

        @Override
        public JsonDeserializer<Object> getContentDeserializer() {
            return null;
        }

        @Override
        public Links deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            JavaType type = ctxt.getTypeFactory().constructMapType(HashMap.class, String.class, String.class);
            List<Link> links = new ArrayList<>();
            Map<String, String> jsonApiLinks = jp.getCodec().readValue(jp, type);
            jsonApiLinks.forEach((rel, href) -> links.add(Link.of(href, rel)));
            return Links.of(links);
        }
    }
}
