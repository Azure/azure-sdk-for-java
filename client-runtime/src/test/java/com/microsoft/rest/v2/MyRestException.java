package com.microsoft.rest.v2;

import com.microsoft.rest.v2.entities.HttpBinJSON;
import com.microsoft.rest.v2.http.HttpResponse;

public class MyRestException extends RestException {
    public MyRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON body() {
        return (HttpBinJSON) super.body();
    }
}