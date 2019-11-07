// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.batch;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;

import java.util.HashSet;
import java.util.Set;

/**
 * REST response associated to a Azure Storage Blob batch operation.
 *
 * @param <T> The deserialized type of the response content, available from {@link #getValue()}.
 */
final class BlobBatchOperationResponse<T> implements Response<T> {
    private final ClientLogger logger = new ClientLogger(BlobBatchOperationResponse.class);

    private final Set<Integer> expectedStatusCodes;

    private int statusCode;
    private HttpHeaders headers;
    private HttpRequest request;
    private T value;
    private BlobStorageException exception;

    private boolean responseReceived = false;

    BlobBatchOperationResponse(int... expectedStatusCodes) {
        this.expectedStatusCodes = new HashSet<>();
        for (int expectedStatusCode : expectedStatusCodes) {
            this.expectedStatusCodes.add(expectedStatusCode);
        }
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

    BlobBatchOperationResponse<T> setException(BlobStorageException exception) {
        this.exception = exception;
        return this;
    }

    boolean wasExpectedResponse() {
        return expectedStatusCodes.contains(statusCode);
    }

    HttpResponse asHttpResponse(String body) {
        return BlobBatchHelper.createHttpResponse(request, statusCode, headers, body);
    }

    private void assertResponseReceived() {
        if (!responseReceived) {
            // This is programmatically recoverable by sending the batch request.
            throw logger.logExceptionAsWarning(new UnsupportedOperationException("Batch request has not been sent."));
        }

        if (!expectedStatusCodes.contains(statusCode)) {
            throw logger.logExceptionAsError(exception);
        }
    }
}
