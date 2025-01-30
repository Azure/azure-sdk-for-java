// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.annotation.processor.exceptions;

/**
 * Exception thrown when a substitution is missing from the template.
 */
public class MissingSubstitutionException extends RuntimeException {

    /**
     * Creates a new instance of the exception.
     * @param message The exception message.
     */
    public MissingSubstitutionException(String message) {
        super(message);
    }
}
