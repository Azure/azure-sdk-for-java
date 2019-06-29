// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.storage.blob.models.ServiceGetUserDelegationKeyHeaders;
import com.azure.storage.blob.models.UserDelegationKey;

/**
 * Contains all response data for the getUserDelegationKey operation.
 */
public final class ServicesGetUserDelegationKeyResponse extends ResponseBase<com.azure.storage.blob.models.ServiceGetUserDelegationKeyHeaders, com.azure.storage.blob.models.UserDelegationKey> {
    /**
     * Creates an instance of ServicesGetUserDelegationKeyResponse.
     *
     * @param request the request which resulted in this ServicesGetUserDelegationKeyResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public ServicesGetUserDelegationKeyResponse(HttpRequest request, int statusCode, HttpHeaders rawHeaders, com.azure.storage.blob.models.UserDelegationKey value, ServiceGetUserDelegationKeyHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }

    /**
     * @return the deserialized response body.
     */
    @Override
    public UserDelegationKey value() {
        return super.value();
    }
}
