package com.azure.storage.blob.specialized;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Response;
import reactor.core.publisher.Mono;

public class BlobBatchOperationResponse<T> implements Response<T> {
    private final Mono<? extends Response> response;
    private int contentId;

    private int statusCode;
    private HttpHeaders headers;
    private HttpRequest request;
    private T value;

    BlobBatchOperationResponse(Mono<? extends Response> response) {
        this.response = response;
    }

    /* TODO: This class will need to handle throwing an exception if the value hasn't been returned from the service
     * when it is requested.
     */

    Mono<? extends Response> getResponse() {
        return response;
    }

    void setContentId(int contentId) {
        this.contentId = contentId;
    }

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
