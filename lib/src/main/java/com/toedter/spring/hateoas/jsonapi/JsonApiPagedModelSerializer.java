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

import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Links;
import org.springframework.hateoas.PagedModel;

import java.util.*;

class JsonApiPagedModelSerializer extends AbstractJsonApiModelSerializer<PagedModel<?>> {

    public JsonApiPagedModelSerializer(JsonApiConfiguration jsonApiConfiguration) {
        super(PagedModel.class, false, jsonApiConfiguration);
    }

    @Override
    protected JsonApiDocument postProcess(
            PagedModel<?> pagedModel, JsonApiDocument doc, JsonApiConfiguration jsonApiConfiguration) {

        final PagedModel.PageMetadata metadata = pagedModel.getMetadata();
        if (metadata != null) {
            Map<String, Object> metaMap = new HashMap<>();

            final long pageNumber = metadata.getNumber();
            final long pageSize = metadata.getSize();
            final long totalElements = metadata.getTotalElements();
            final long totalPages = metadata.getTotalPages();

            if(jsonApiConfiguration.isPaginationMetaAutomaticallyCreated()) {
                metaMap.put(Jackson2JsonApiModule.PAGE_NUMBER, pageNumber);
                metaMap.put(Jackson2JsonApiModule.PAGE_SIZE, pageSize);
                metaMap.put(Jackson2JsonApiModule.PAGE_TOTAL_ELEMENTS, totalElements);
                metaMap.put(Jackson2JsonApiModule.PAGE_TOTAL_PAGES, totalPages);
                doc = doc.withMeta(metaMap);
            }

            final Optional<Link> selfLinkOptional = pagedModel.getLink(IanaLinkRelations.SELF);

            if (selfLinkOptional.isPresent() && jsonApiConfiguration.isPaginationLinksAutomaticallyCreated()) {
                final String pageNumberReqestParam = jsonApiConfiguration.getPageNumberRequestParameter();
                final String pageSizeRequestParam = jsonApiConfiguration.getPageSizeRequestParameter();

                final Link selfLink = selfLinkOptional.get();
                final String selfLinkHref = selfLink.getHref();
                List<Link> paginationLinks = new ArrayList<>();

                if (pageNumber > 0) {
                    Link firstLink = Link.of(selfLinkHref + "?" + pageNumberReqestParam + "=0&"
                            + pageSizeRequestParam + "=" + pageSize).withRel(IanaLinkRelations.FIRST);
                    paginationLinks.add(firstLink);
                }

                if (pageNumber > 0) {
                    Link prevLink = Link.of(selfLinkHref + "?" + pageNumberReqestParam + "=" + (pageNumber - 1)
                            + "&" + pageSizeRequestParam + "=" + pageSize).withRel(IanaLinkRelations.PREV);
                    paginationLinks.add(prevLink);
                }

                if (pageNumber < totalPages - 1) {
                    Link nextLink = Link.of(selfLinkHref + "?" + pageNumberReqestParam + "=" + (pageNumber + 1)
                            + "&" + pageSizeRequestParam + "=" + (pageNumber + 1)).withRel(IanaLinkRelations.NEXT);
                    paginationLinks.add(nextLink);
                }

                if (pageNumber < totalPages - 1) {
                    Link lastLink = Link.of(selfLinkHref + "?" + pageNumberReqestParam + "=" + (totalPages - 1)
                            + "&" + pageSizeRequestParam + "=" + pageSize).withRel(IanaLinkRelations.LAST);
                    paginationLinks.add(lastLink);
                }

                doc = doc.withLinks(pagedModel.getLinks().merge(paginationLinks));
            }
        }
        return doc;
    }
}
