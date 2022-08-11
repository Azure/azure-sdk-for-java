// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormRecognizerErrorInformation model.
 */
@Immutable
public final class FormRecognizerErrorInformation {
    /*
     * The errorCode property.
     */
    private final String errorCode;

    /*
     * The message property.
     */
    private final String message;

    /**
     * Constructs a FormRecognizerErrorInformation object.
     *
     * @param errorCode the error code returned by the service.
     * @param message the error message returned by the service
     */
    public FormRecognizerErrorInformation(final String errorCode, final String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Get the error code property returned by the service.
     *
     * @return the error code property returned by the service.
     */
    public String getErrorCode() {
        return this.errorCode;
    }


    /**
     * Get the message property returned by the service.
     *
     * @return the message property returned by the service.
     */
    public String getMessage() {
        return this.message;
    }
}
