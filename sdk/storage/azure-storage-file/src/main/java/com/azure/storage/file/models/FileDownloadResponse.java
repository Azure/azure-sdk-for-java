// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.models;

import com.azure.core.http.rest.ResponseBase;

/**
 * This class contains the response information returned from the server when downloading a file.
 */
public class FileDownloadResponse extends ResponseBase<FileDownloadHeaders, Void> {
    /**
     * Constructs a {@link FileDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public FileDownloadResponse(FileDownloadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
