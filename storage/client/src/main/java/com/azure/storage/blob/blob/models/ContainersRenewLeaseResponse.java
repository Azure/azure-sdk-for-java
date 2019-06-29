// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.storage.blob.models.ContainerRenewLeaseHeaders;

/**
 * Contains all response data for the renewLease operation.
 */
public final class ContainersRenewLeaseResponse extends ResponseBase<com.azure.storage.blob.models.ContainerRenewLeaseHeaders, Void> {
    /**
     * Creates an instance of ContainersRenewLeaseResponse.
     *
     * @param request the request which resulted in this ContainersRenewLeaseResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public ContainersRenewLeaseResponse(HttpRequest request, int statusCode, HttpHeaders rawHeaders, Void value, ContainerRenewLeaseHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
