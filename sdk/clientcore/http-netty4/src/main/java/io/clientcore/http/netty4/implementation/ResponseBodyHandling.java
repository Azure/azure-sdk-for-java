// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.utils.ServerSentEventUtils;

import static io.clientcore.core.http.models.HttpMethod.HEAD;

public enum ResponseBodyHandling {
    /**
     * Response body is ignored. The HTTP response will be drained and ignored.
     */
    IGNORE,

    /**
     * Response body should be streamed while it's being consumed. The HTTP response will be kept open until either the
     * response body has been consumed or the response has been closed.
     */
    STREAM,

    /**
     * Response body should be read into memory. The HTTP response will be drained and stored as a byte array in memory.
     */
    BUFFER;

    /**
     * Determines how to handle the HTTP response body.
     *
     * @param request The HTTP request sent that triggered the response.
     * @param responseHeaders The HTTP response headers.
     * @return How to handle the HTTP response body.
     */
    public static ResponseBodyHandling getBodyHandling(HttpRequest request, HttpHeaders responseHeaders) {
        String contentType = responseHeaders.getValue(HttpHeaderName.CONTENT_TYPE);

        if (request.getHttpMethod() == HEAD) {
            return ResponseBodyHandling.IGNORE;
        } else if ("application/octet-stream".equalsIgnoreCase(contentType)
            || ServerSentEventUtils.isTextEventStreamContentType(contentType)) {
            return ResponseBodyHandling.STREAM;
        } else {
            return ResponseBodyHandling.BUFFER;
        }
    }
}
