// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.mgmt;

import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpResponse;

public class MyAzureException extends ServiceRequestException {
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
    public HttpBinJSON value() {
        return (HttpBinJSON) super.value();
    }
}
