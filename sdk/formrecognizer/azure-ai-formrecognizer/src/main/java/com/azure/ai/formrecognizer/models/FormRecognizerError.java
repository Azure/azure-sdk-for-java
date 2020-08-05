// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The FormRecognizerError model.
 */
@Immutable
public final class FormRecognizerError {
    /*
     * The error code property.
     */
    private final String errorCode;

    /*
     * The message property.
     */
    private final String message;

    /**
     * Constructs a FormRecognizerError object.
     *
     * @param errorCode The error code property.
     * @param message The message property.
     */
    public FormRecognizerError(final String errorCode, final String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    /**
     * Get the code property.
     *
     * @return the error code value returned by the service.
     */
    public String getErrorCode() {
        return this.errorCode;
    }


    /**
     * Get the message property.
     *
     * @return the message value returned by the service.
     */
    public String getMessage() {
        return this.message;
    }

}
