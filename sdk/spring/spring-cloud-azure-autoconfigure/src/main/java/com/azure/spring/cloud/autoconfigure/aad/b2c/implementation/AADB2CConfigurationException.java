// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.aad.b2c.implementation;

/**
 * Throw runtime exception for configuration.
 */
public class AADB2CConfigurationException extends RuntimeException {

    public AADB2CConfigurationException(String message) {
        super(message);
    }

    public AADB2CConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
