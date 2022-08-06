// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

class AppConfigurationStatusException extends HttpResponseException {

    AppConfigurationStatusException(final String message, final HttpResponse response, final Object value) {
        super(message, response, value);
    }

    private static final long serialVersionUID = -2388602959090868645L;

   

}
