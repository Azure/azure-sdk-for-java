// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.client.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/** Contains all response data for the createGroup operation. */
public final class CreateGroupResponse extends ResponseBase<CreateGroupHeaders, Void> {
    /**
     * Creates an instance of CreateGroupResponse.
     *
     * @param request the request which resulted in this CreateGroupResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public CreateGroupResponse(
            HttpRequest request, int statusCode, HttpHeaders rawHeaders, Void value, CreateGroupHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
