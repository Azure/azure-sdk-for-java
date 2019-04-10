package com.azure.common.mgmt;

import com.azure.common.http.rest.RestException;
import com.azure.common.http.HttpResponse;

public class MyAzureException extends RestException {
    public MyAzureException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}