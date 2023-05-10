// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.spring.data.cosmos.core.convert.ObjectMapperFactory;
import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PageTestUtils {

    private static final ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    public static void validateLastPage(Page<?> page, int pageSize) {
        final Pageable pageable = page.getPageable();

        assertThat(pageable).isInstanceOf(CosmosPageRequest.class);
        assertTrue(continuationTokenIsNull((CosmosPageRequest) pageable));
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);
        assertThat(page.hasNext()).isFalse();
    }

    public static void validateNonLastPage(Page<?> page, int pageSize) {
        final Pageable pageable = page.getPageable();

        assertThat(pageable).isInstanceOf(CosmosPageRequest.class);
        assertThat(((CosmosPageRequest) pageable).getRequestContinuation()).isNotNull();
        assertThat(((CosmosPageRequest) pageable).getRequestContinuation()).isNotBlank();
        assertThat(pageable.getPageSize()).isEqualTo(pageSize);
        assertThat(page.hasNext()).isTrue();
    }

    private static boolean continuationTokenIsNull(CosmosPageRequest pageRequest) {
        final String tokenJson = pageRequest.getRequestContinuation();
        if (tokenJson == null) {
            return true;
        }
        try {
            JsonNode jsonNode = objectMapper.readTree(tokenJson);
            return jsonNode.get("compositeToken") == null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }
}
