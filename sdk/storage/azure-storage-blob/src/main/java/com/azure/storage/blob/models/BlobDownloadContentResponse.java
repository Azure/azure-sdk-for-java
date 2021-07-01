// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.rest.ResponseBase;
import com.azure.core.util.BinaryData;

/**
 * This class contains the response information return from the server when downloading a blob.
 */
public final class BlobDownloadContentResponse extends ResponseBase<BlobDownloadHeaders, BinaryData> {
    /**
     * Constructs a {@link BlobDownloadContentResponse}.
     *
     * @param response Response returned from the service.
     */
    public BlobDownloadContentResponse(BlobDownloadContentAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(),
            response.getDeserializedHeaders());
    }
}
