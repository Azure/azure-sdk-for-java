// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.primitives;

/**
 * This exception is thrown when the connection string provided does not meet the requirement for connection.
 * @since 1.0
 */
public class IllegalConnectionStringFormatException extends IllegalArgumentException {
    private static final long serialVersionUID = 2514898858133972030L;

    IllegalConnectionStringFormatException() {
    }

    IllegalConnectionStringFormatException(String detail) {
        super(detail);
    }

    IllegalConnectionStringFormatException(Throwable cause) {
        super(cause);
    }

    IllegalConnectionStringFormatException(String detail, Throwable cause) {
        super(detail, cause);
    }

}
