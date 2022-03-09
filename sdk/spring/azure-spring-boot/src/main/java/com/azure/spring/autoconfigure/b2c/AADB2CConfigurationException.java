// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.autoconfigure.b2c;

/**
 * Throw runtime exception for configuration.
 */
public class AADB2CConfigurationException extends RuntimeException {

    /**
     * Creates a new instance of {@link AADB2CConfigurationException}.
     *
     * @param message the exception message
     */
    public AADB2CConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of {@link AADB2CConfigurationException}.
     *
     * @param message the exception message
     * @param cause the cause
     */
    public AADB2CConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
