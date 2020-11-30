// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.rest.ResponseBase;

/**
 * This class contains the response information return from the server when querying a blob.
 */
public final class BlobQueryResponse extends ResponseBase<BlobQueryHeaders, Void> {
    /**
     * Constructs a {@link BlobQueryResponse}.
     *
     * @param response Response returned from the service.
     */
    public BlobQueryResponse(BlobQueryAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
