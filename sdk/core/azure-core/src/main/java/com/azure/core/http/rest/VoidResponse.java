// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;

/**
 * REST response containing only a status code and raw headers.
 */
public final class VoidResponse extends SimpleResponse<Void> {
    /**
     * Creates VoidResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param headers the headers of the HTTP response
     */
    public VoidResponse(HttpRequest request, int statusCode, HttpHeaders headers) {
        super(request, statusCode, headers, null);
    }

    /**
     * Creates VoidResponse.
     *
     * @param response a response used to construct the void response.
     */
    public VoidResponse(Response<?> response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
    }
}
