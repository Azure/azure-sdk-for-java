package com.microsoft.azure;

import com.microsoft.rest.RestException;
import com.microsoft.rest.http.HttpResponse;

public class MyAzureException extends RestException {
    public MyAzureException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}