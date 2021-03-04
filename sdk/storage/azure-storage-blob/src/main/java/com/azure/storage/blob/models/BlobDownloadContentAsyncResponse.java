// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;

/**
 * This class contains the response information returned from the server when downloading a blob.
 */
public final class BlobDownloadContentAsyncResponse extends ResponseBase<BlobDownloadHeaders, BinaryData> {
    /**
     * Constructs a {@link BlobDownloadContentAsyncResponse}.
     *
     * @param request Request sent to the service.
     * @param statusCode Response status code returned by the service.
     * @param headers Raw headers returned in the response.
     * @param value Stream of download data being returned by the service.
     * @param deserializedHeaders Headers deserialized into an object.
     */
    public BlobDownloadContentAsyncResponse(HttpRequest request, int statusCode, HttpHeaders headers, BinaryData value,
                                            BlobDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, value, deserializedHeaders);
    }
}
