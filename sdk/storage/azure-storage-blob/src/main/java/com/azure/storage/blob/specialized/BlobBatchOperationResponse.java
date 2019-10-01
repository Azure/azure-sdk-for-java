package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;

public class BlobBatchOperationResponse<T> implements Response<T> {
    private final int contentId;

    private int statusCode;
    private HttpHeaders headers;
    private HttpRequest request;
    private T value;

    BlobBatchOperationResponse(int contentId) {
        this.contentId = contentId;
    }

    /* TODO: This class will need to handle throwing an exception if the value hasn't been returned from the service
     * when it is requested.
     */

    @Override
    public int getStatusCode() {
        return statusCode;
    }

    BlobBatchOperationResponse<T> setStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    BlobBatchOperationResponse<T> setHeaders(HttpHeaders headers) {
        this.headers = headers;
        return this;
    }

    @Override
    public HttpRequest getRequest() {
        return request;
    }

    BlobBatchOperationResponse<T> setRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    @Override
    public T getValue() {
        return value;
    }

    BlobBatchOperationResponse<T> setValue(T value) {
        this.value = value;
        return this;
    }
}
