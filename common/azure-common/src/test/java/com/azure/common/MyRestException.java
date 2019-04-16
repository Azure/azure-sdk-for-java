// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common;

import com.azure.common.entities.HttpBinJSON;
import com.azure.common.exception.ServiceRequestException;
import com.azure.common.http.HttpResponse;

public class MyRestException extends ServiceRequestException {
    public MyRestException(String message, HttpResponse response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON value() {
        return (HttpBinJSON) super.value();
    }
}
