// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aadb2c.implementation;

/**
 * Throw runtime exception for configuration.
 */
public class AadB2CConfigurationException extends RuntimeException {

    /**
     * Creates a new instance of {@link AadB2CConfigurationException}.
     *
     * @param message the exception message
     */
    public AadB2CConfigurationException(String message) {
        super(message);
    }

    /**
     * Creates a new instance of {@link AadB2CConfigurationException}.
     *
     * @param message the exception message
     * @param cause the cause
     */
    public AadB2CConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
