/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest;

/**
 * The REST response object that is a result of making a REST request.
 * @param <TBody> The deserialized type of the response body.
 * @param <THeaders> The deserialized type of the response headers.
 */
public class RestResponseWithBody<THeaders, TBody> extends RestResponse<THeaders> {
    private final TBody body;

    /**
     * Create a new RestResponseWithBody object.
     * @param statusCode The status code for the REST response.
     * @param headers The deserialized headers.
     * @param body The deserialized body.
     */
    public RestResponseWithBody(int statusCode, THeaders headers, TBody body) {
        super(statusCode, headers);

        this.body = body;
    }

    /**
     * The deserialized body of the HTTP response.
     * @return The deserialized body of the HTTP response.
     */
    public TBody body() {
        return body;
    }
}
