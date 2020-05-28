// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import java.util.List;

/** Contains all response data for the getSchemasByGroup operation. */
public final class GetSchemasByGroupResponse extends ResponseBase<GetSchemasByGroupHeaders, List<String>> {
    /**
     * Creates an instance of GetSchemasByGroupResponse.
     *
     * @param request the request which resulted in this GetSchemasByGroupResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public GetSchemasByGroupResponse(
            HttpRequest request,
            int statusCode,
            HttpHeaders rawHeaders,
            List<String> value,
            GetSchemasByGroupHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return the deserialized response body. */
    @Override
    public List<String> getValue() {
        return super.getValue();
    }
}
