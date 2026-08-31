// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.http.models;

import io.clientcore.core.http.paging.PagedResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

public class PagedResponseTests {

    @Test
    public void testPagedResponseRequired() {
        final HttpRequest mockHttpRequest = new HttpRequest().setMethod(HttpMethod.GET).setUri("https://endpoint");
        final int statusCode = 200;
        final HttpHeaders mockHttpHeaders = new HttpHeaders();
        final List<Object> mockValue = Collections.singletonList(new Object());

        final String continuationToken = "continuation_token";
        final String nextLink = "https://next_link";
        final String previousLink = "https://previous_link";
        final String firstLink = "https://first_link";
        final String lastLink = "https://last_link";

        try (PagedResponse<Object, String> pagedResponse
            = new PagedResponse<>(mockHttpRequest, statusCode, mockHttpHeaders, mockValue)) {
            Assertions.assertEquals(mockHttpRequest, pagedResponse.getRequest());
            Assertions.assertEquals(statusCode, pagedResponse.getStatusCode());
            Assertions.assertEquals(mockHttpHeaders, pagedResponse.getHeaders());
            Assertions.assertEquals(mockValue, pagedResponse.getValue());
        }

        try (PagedResponse<Object, String> pagedResponse = new PagedResponse<>(mockHttpRequest, statusCode,
            mockHttpHeaders, mockValue, continuationToken, nextLink, previousLink, firstLink, lastLink)) {
            Assertions.assertEquals(mockHttpRequest, pagedResponse.getRequest());
            Assertions.assertEquals(statusCode, pagedResponse.getStatusCode());
            Assertions.assertEquals(mockHttpHeaders, pagedResponse.getHeaders());
            Assertions.assertEquals(mockValue, pagedResponse.getValue());

            Assertions.assertEquals(continuationToken, pagedResponse.getContinuationToken());
            Assertions.assertEquals(nextLink, pagedResponse.getNextLink());
            Assertions.assertEquals(previousLink, pagedResponse.getPreviousLink());
            Assertions.assertEquals(firstLink, pagedResponse.getFirstLink());
            Assertions.assertEquals(lastLink, pagedResponse.getLastLink());
        }
    }

    @Test
    public void supportsNonStringContinuationToken() {
        HttpRequest request = new HttpRequest().setMethod(HttpMethod.GET).setUri("https://endpoint");
        Object continuationToken = new Object();

        try (PagedResponse<Object, Object> response = new PagedResponse<>(request, 200, new HttpHeaders(),
            Collections.emptyList(), continuationToken, null, null, null, null)) {
            Object actual = response.getContinuationToken();
            Assertions.assertSame(continuationToken, actual);
        }
    }
}
