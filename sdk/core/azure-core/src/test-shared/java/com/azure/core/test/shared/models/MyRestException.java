// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.shared.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

public class MyRestException extends HttpResponseException {
    /**
     * Creates a service request exception.
     *
     * @param message Message associated with exception.
     * @param response HttpResponse associated with the exception.
     * @param body Deserialized body of {@code response}.
     */
    public MyRestException(String message, HttpResponse response, HttpBinJson body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJson getValue() {
        return (HttpBinJson) super.getValue();
    }
}
