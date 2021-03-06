// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.iot.deviceupdate.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/** Contains all response data for the deleteUpdate operation. */
public final class UpdatesDeleteUpdateResponse extends ResponseBase<UpdatesDeleteUpdateHeaders, Void> {
    /**
     * Creates an instance of UpdatesDeleteUpdateResponse.
     *
     * @param request the request which resulted in this UpdatesDeleteUpdateResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public UpdatesDeleteUpdateResponse(
            HttpRequest request,
            int statusCode,
            HttpHeaders rawHeaders,
            Void value,
            UpdatesDeleteUpdateHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
