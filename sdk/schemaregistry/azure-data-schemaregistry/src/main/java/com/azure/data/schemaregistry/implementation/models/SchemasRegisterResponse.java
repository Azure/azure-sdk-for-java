// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry.implementation.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/** Contains all response data for the register operation. */
public final class SchemasRegisterResponse extends ResponseBase<SchemasRegisterHeaders, SchemaId> {
    /**
     * Creates an instance of SchemasRegisterResponse.
     *
     * @param request the request which resulted in this SchemasRegisterResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public SchemasRegisterResponse(
            HttpRequest request,
            int statusCode,
            HttpHeaders rawHeaders,
            SchemaId value,
            SchemasRegisterHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /** @return the deserialized response body. */
    @Override
    public SchemaId getValue() {
        return super.getValue();
    }
}
