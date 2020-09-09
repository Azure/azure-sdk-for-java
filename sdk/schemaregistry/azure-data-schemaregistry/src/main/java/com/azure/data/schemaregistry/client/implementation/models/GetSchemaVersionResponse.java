// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/** Contains all response data for the getSchemaVersion operation. */
public final class GetSchemaVersionResponse extends ResponseBase<GetSchemaVersionHeaders, String> {
    /**
     * Creates an instance of GetSchemaVersionResponse.
     *
     * @param request the request which resulted in this GetSchemaVersionResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public GetSchemaVersionResponse(
            HttpRequest request,
            int statusCode,
            HttpHeaders rawHeaders,
            String value,
            GetSchemaVersionHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return the deserialized response body. */
    @Override
    public String getValue() {
        return super.getValue();
    }
}
