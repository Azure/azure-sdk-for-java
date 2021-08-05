// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/**
 * A helper class to represent the HTTP response returned by the service.
 */
public final class DigitalTwinsResponse<T> extends ResponseBase<DigitalTwinsResponseHeaders, T> {
    /**
     * Creates an instance of DigitalTwinsResponse.
     *
     * @param request the request which resulted in this DigitalTwinsResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the raw value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public DigitalTwinsResponse(
        HttpRequest request, int statusCode, HttpHeaders rawHeaders, T value, DigitalTwinsResponseHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return The raw response body. */
    @Override
    public T getValue() {
        return super.getValue();
    }
}
