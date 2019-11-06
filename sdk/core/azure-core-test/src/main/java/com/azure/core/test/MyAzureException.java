// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.implementation.entities.HttpBinJSON;

/**
 * A test exception.
 */
public class MyAzureException extends HttpResponseException {
    /**
     * Creates an exception with the {@code message}, {@code response}, and {@code body}.
     *
     * @param message Message for the exception.
     * @param response HttpResponse associated with the service request exception.
     * @param body HttpResponse deserialized into a {@link HttpBinJSON}.
     */
    public MyAzureException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON getValue() {
        return (HttpBinJSON) super.getValue();
    }
}
