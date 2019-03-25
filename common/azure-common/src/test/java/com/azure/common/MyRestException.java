package com.azure.common;

import com.azure.common.entities.HttpBinJSON;
import com.azure.common.http.HttpResponse;
import com.azure.common.http.rest.RestException;

public class MyRestException extends RestException {
    public MyRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}