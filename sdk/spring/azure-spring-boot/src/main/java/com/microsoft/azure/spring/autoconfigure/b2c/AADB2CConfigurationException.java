/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.b2c;

public class AADB2CConfigurationException extends RuntimeException {

    public AADB2CConfigurationException(String message) {
        super(message);
    }

    public AADB2CConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
