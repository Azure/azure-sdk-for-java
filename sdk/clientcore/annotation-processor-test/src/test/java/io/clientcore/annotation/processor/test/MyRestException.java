// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.test;


import io.clientcore.annotation.processor.test.implementation.models.HttpBinJSON;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.models.binarydata.BinaryData;

public class MyRestException extends HttpResponseException {
    /**
     * Creates a service request exception.
     *
     * @param message Message associated with exception.
     * @param response HttpResponse associated with the exception.
     * @param body Deserialized body of {@code response}.
     */
    public MyRestException(String message, Response<BinaryData> response, HttpBinJSON body) {
        super(message, response, body);
    }

    @Override
    public HttpBinJSON getValue() {
        return (HttpBinJSON) super.getValue();
    }
}
