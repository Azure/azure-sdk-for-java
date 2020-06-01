// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import java.util.List;

/** Contains all response data for the getSchemaVersions operation. */
public final class GetSchemaVersionsResponse extends ResponseBase<GetSchemaVersionsHeaders, List<Integer>> {
    /**
     * Creates an instance of GetSchemaVersionsResponse.
     *
     * @param request the request which resulted in this GetSchemaVersionsResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public GetSchemaVersionsResponse(
            HttpRequest request,
            int statusCode,
            HttpHeaders rawHeaders,
            List<Integer> value,
            GetSchemaVersionsHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return the deserialized response body. */
    @Override
    public List<Integer> getValue() {
        return super.getValue();
    }
}
