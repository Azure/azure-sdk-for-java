package com.azure.common.mgmt;

import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpResponse;

public class MyAzureException extends ServiceRequestException {
    public MyAzureException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON result() {
        return (HttpBinJSON) super.result();
    }
}
