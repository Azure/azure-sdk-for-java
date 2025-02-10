// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import com.azure.cosmos.implementation.directconnectivity.TransportException;

public class ClosedClientTransportException extends TransportException {
    public ClosedClientTransportException(String message, Throwable cause) {
        super(message, cause);
    }
}
