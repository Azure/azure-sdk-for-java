package com.azure.spring.cloud.config.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

class AppConfigurationStatusException extends HttpResponseException {

    public AppConfigurationStatusException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    private static final long serialVersionUID = -2388602959090868645L;

   

}
