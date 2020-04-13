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
     * The code property.
     */
    private final String code;

    /*
     * The message property.
     */
    private final String message;

    /**
     * Constructs a FormRecognizerError object.
     *
     * @param code The code property.
     * @param message The message property.
     */
    public FormRecognizerError(final String code, final String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get the code property: The code property.
     *
     * @return the code value.
     */
    public String getCode() {
        return this.code;
    }


    /**
     * Get the message property: The message property.
     *
     * @return the message value.
     */
    public String getMessage() {
        return this.message;
    }

}
