// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core;

import com.azure.core.entities.HttpBinJSON;
import com.azure.core.exception.HttpRequestException;
import com.azure.core.http.HttpResponse;

public class MyRestException extends HttpRequestException {
    /**
     * Creates a service request exception.
     *
     * @param message Message associated with exception.
     * @param response HttpResponse associated with the exception.
     * @param body Deserialized body of {@code response}.
     */
    public MyRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON value() {
        return (HttpBinJSON) super.value();
    }
}
