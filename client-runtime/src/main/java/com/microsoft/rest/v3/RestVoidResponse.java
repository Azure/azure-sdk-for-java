/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v3;

import com.microsoft.rest.v3.http.HttpRequest;

import java.util.Map;

/**
 * REST response containing only a status code and raw headers.
 */
public final class RestVoidResponse extends RestResponse<Void, Void> {
    /**
     * Creates RestVoidResponse.
     *
     * @param request the request which resulted in this response
     * @param statusCode the status code of the HTTP response
     * @param rawHeaders the raw headers of the HTTP response
     */
    public RestVoidResponse(HttpRequest request, int statusCode, Map<String, String> rawHeaders) {
        super(request, statusCode, null, rawHeaders, null);
    }

    // Used for uniform reflective creation in RestProxy.
    @SuppressWarnings("unused")
    RestVoidResponse(HttpRequest request, int statusCode, Void headers, Map<String, String> rawHeaders, Void body) {
        super(request, statusCode, headers, rawHeaders, body);
    }

    /**
     * Always returns null due to no headers type being defined in the service specification.
     * Consider using {@link #rawHeaders()}.
     *
     * @return null
     */
    @Override
    public Void headers() {
        return super.headers();
    }

    /**
     * @return null due to no body type being defined in the service specification
     */
    @Override
    public Void body() {
        return super.body();
    }
}
