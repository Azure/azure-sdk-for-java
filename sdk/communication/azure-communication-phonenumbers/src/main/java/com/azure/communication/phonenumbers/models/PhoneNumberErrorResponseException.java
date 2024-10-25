// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.models;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpResponse;

/**
 * Exception thrown on phone number specific errors
 */
public class PhoneNumberErrorResponseException extends HttpResponseException {
    /**
     * Initializes a new instance of the PhoneNumberErrorResponseException class.
     *
     * @param message the exception message or the response content if a message is not available.
     * @param response the HTTP response.
     * @param value the deserialized response value.
     */
    public PhoneNumberErrorResponseException(
        String message, HttpResponse response, PhoneNumberError value) {
        super(message, response, value);
    }

    @Override
    public PhoneNumberError getValue() {
        return (PhoneNumberError) super.getValue();
    }
}
