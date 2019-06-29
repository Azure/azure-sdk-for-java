// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.storage.blob.models.BlobStartCopyFromURLHeaders;

/**
 * Contains all response data for the startCopyFromURL operation.
 */
public final class BlobsStartCopyFromURLResponse extends ResponseBase<com.azure.storage.blob.models.BlobStartCopyFromURLHeaders, Void> {
    /**
     * Creates an instance of BlobsStartCopyFromURLResponse.
     *
     * @param request the request which resulted in this BlobsStartCopyFromURLResponse.
     * @param statusCode the status code of the HTTP response.
     * @param rawHeaders the raw headers of the HTTP response.
     * @param value the deserialized value of the HTTP response.
     * @param headers the deserialized headers of the HTTP response.
     */
    public BlobsStartCopyFromURLResponse(HttpRequest request, int statusCode, HttpHeaders rawHeaders, Void value, BlobStartCopyFromURLHeaders headers) {
        super(request, statusCode, rawHeaders, value, headers);
    }
}
