// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.models;

import com.azure.core.http.rest.ResponseBase;

/**
 * This class contains the response information return from the server when reading a file.
 */
public final class FileReadResponse extends ResponseBase<FileReadHeaders, Void> {
    /**
     * Constructs a {@link FileReadResponse}.
     *
     * @param response Response returned from the service.
     */
    public FileReadResponse(FileReadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
