// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;

import java.io.InputStream;

/**
 * This class contains the response information returned from the server when downloading a file.
 */
public class ShareFileDownloadResponse extends ResponseBase<ShareFileDownloadHeaders, Void> {
    /**
     * Constructs a {@link ShareFileDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public ShareFileDownloadResponse(ShareFileDownloadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }

    /**
     * Constructs a {@link ShareFileDownloadResponse}.
     *
     * @param request – The HTTP request which resulted in this response.
     * @param statusCode – The status code of the HTTP response.
     * @param headers – The headers of the HTTP response.
     * @param value – The deserialized value of the HTTP response.
     * @param deserializedHeaders – The deserialized headers of the HTTP response.
     *
     */
    public ShareFileDownloadResponse(HttpRequest request, int statusCode, HttpHeaders headers, InputStream value,
        ShareFileDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, null, deserializedHeaders);
    }
}
