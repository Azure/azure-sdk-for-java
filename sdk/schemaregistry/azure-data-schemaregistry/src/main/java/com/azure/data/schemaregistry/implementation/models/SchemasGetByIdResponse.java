// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/** Contains all response data for the getById operation. */
public final class SchemasGetByIdResponse extends ResponseBase<SchemasGetByIdHeaders, String> {
    /**
     * Creates an instance of SchemasGetByIdResponse.
     *
     * @param request the request which resulted in this SchemasGetByIdResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public SchemasGetByIdResponse(
            HttpRequest request, int statusCode, HttpHeaders rawHeaders, String value, SchemasGetByIdHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return the deserialized response body. */
    @Override
    public String getValue() {
        return super.getValue();
    }
}
