// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.http.rest.ResponseBase;

/**
 * This class contains the response information return from the server when querying a file.
 */
public final class FileQueryResponse extends ResponseBase<FileQueryHeaders, Void> {
    /**
     * Constructs a {@link FileQueryResponse}.
     *
     * @param response Response returned from the service.
     */
    public FileQueryResponse(FileQueryAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
