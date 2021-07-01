// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

public class PageTestUtils {
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

        final JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(tokenJson);
            return jsonObject.isNull("compositeToken");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }
}
