// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

/**
 * This class contains the response information return from the server when downloading a blob.
 */
public final class BlobDownloadResponse extends ResponseBase<BlobDownloadHeaders, Void> {
    /**
     * Constructs a {@link BlobDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public BlobDownloadResponse(BlobDownloadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }

    /**
     * Constructs a {@link BlobDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public BlobDownloadResponse(BlobDownloadSyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }

    /**
     * Constructs a {@link BlobDownloadResponse}.
     * @param request request.
     * @param statusCode status code.
     * @param headers headers.
     * @param deserializedHeaders deserialized headers.
     */
    public BlobDownloadResponse(
        HttpRequest request, int statusCode, HttpHeaders headers, BlobDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, null, deserializedHeaders);
    }
}
