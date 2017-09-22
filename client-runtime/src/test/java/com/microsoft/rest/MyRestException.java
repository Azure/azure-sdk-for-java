package com.microsoft.rest;

import com.microsoft.rest.http.HttpResponse;

public class MyRestException extends RestException {
    public MyRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}