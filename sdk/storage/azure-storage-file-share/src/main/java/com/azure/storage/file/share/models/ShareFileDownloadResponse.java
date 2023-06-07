// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.models;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.ResponseBase;
import reactor.core.publisher.Flux;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class contains the response information returned from the server when downloading a file.
 */
public class ShareFileDownloadResponse extends ResponseBase<ShareFileDownloadHeaders, Void> {

    /**
     * Constructs a {@link ShareFileDownloadAsyncResponse}.
     *
     * @param request Request sent to the service.
     * @param statusCode Response status code returned by the service.
     * @param headers Raw headers returned in the response.
     * @param deserializedHeaders Headers deserialized into an object.
     */
    public ShareFileDownloadResponse(HttpRequest request, int statusCode, HttpHeaders headers,
        ShareFileDownloadHeaders deserializedHeaders) {
        super(request, statusCode, headers, null, deserializedHeaders);
    }

    /**
     * Constructs a {@link ShareFileDownloadResponse}.
     *
     * @param response Response returned from the service.
     */
    public ShareFileDownloadResponse(ShareFileDownloadAsyncResponse response) {
        super(response.getRequest(), response.getStatusCode(), response.getHeaders(), null,
            response.getDeserializedHeaders());
    }
}
