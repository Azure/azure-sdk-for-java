// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;

import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

/**
 * A mock implementation of {@link Response} that can be used for testing.
 */
public class MockHttpResponse extends Response<BinaryData> {

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and has an empty
     * response body.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode) {
        this(request, statusCode, BinaryData.empty());
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, and response body of
     * {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param body Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, BinaryData body) {
        this(request, statusCode, new HttpHeaders(), body);
    }

    /**
     * Creates an HTTP response associated with a {@code request}, returns the {@code statusCode}, contains the
     * {@code headers}, and response body of {@code bodyBytes}.
     *
     * @param request HttpRequest associated with the response.
     * @param statusCode Status code of the response.
     * @param headers HttpHeaders of the response.
     * @param body Contents of the response.
     */
    public MockHttpResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData body) {
        super(request, statusCode, headers, body);
    }

}
