package com.azure.common;

import com.azure.common.entities.HttpBinJSON;
import com.azure.common.http.HttpResponse;
import com.azure.common.exception.ServiceRequestException;

public class MyOtherRestException extends ServiceRequestException {
    public MyOtherRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON value() {
        return (HttpBinJSON) super.value();
    }
}
