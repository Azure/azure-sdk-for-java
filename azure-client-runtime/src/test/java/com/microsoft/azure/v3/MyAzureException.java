package com.microsoft.azure.v3;

import com.microsoft.rest.v3.RestException;
import com.microsoft.rest.v3.http.HttpResponse;

public class MyAzureException extends RestException {
    public MyAzureException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}