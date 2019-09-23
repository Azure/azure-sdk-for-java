package com.azure.core.http.rest;

public interface BatchOperation<T> {
    T getValue(BatchResult response);
    Response<T> getRawResponse(BatchResult response);
}
