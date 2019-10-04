// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.StorageException;

import java.util.Arrays;

public class BlobBatchOperationResponse<T> implements Response<T> {
    private final ClientLogger logger = new ClientLogger(BlobBatchOperationResponse.class);

    private final int[] expectedStatusCodes;

    private int statusCode;
    private HttpHeaders headers;
    private HttpRequest request;
    private T value;
    private StorageException exception;

    private boolean responseReceived = false;

    BlobBatchOperationResponse(int... expectedStatusCodes) {
        this.expectedStatusCodes = expectedStatusCodes;
    }

    @Override
    public int getStatusCode() {
        assertResponseReceived();
        return statusCode;
    }

    BlobBatchOperationResponse<T> setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public HttpHeaders getHeaders() {
        assertResponseReceived();
        return headers;
    }

    BlobBatchOperationResponse<T> setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HttpRequest getRequest() {
        assertResponseReceived();
        return request;
    }

    BlobBatchOperationResponse<T> setRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public T getValue() {
        assertResponseReceived();
        return value;
    }

    BlobBatchOperationResponse<T> setValue(T value) {
        this.value = value;
        return this;
    }

    BlobBatchOperationResponse<T> setResponseReceived() {
        this.responseReceived = true;
        return this;
    }

    BlobBatchOperationResponse<T> setException(StorageException exception) {
        this.exception = exception;
        return this;
    }

    private void assertResponseReceived() {
        if (!responseReceived) {
            // This is programmatically recoverable by sending the batch request.
            throw logger.logExceptionAsWarning(new UnsupportedOperationException("Batch request has not been sent."));
        }

        if (Arrays.stream(expectedStatusCodes).noneMatch(expectedStatusCode -> expectedStatusCode == statusCode)) {
            throw logger.logExceptionAsError(exception);
        }
    }
}
