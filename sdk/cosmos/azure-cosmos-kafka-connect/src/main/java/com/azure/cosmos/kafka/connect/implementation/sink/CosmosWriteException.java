// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.errors.ConnectException;

/**
 * Generic CosmosDb sink write exceptions.
 */
public class CosmosWriteException extends ConnectException {
    private static final long serialVersionUID = 1L;

    public CosmosWriteException(String message) {
        super(message);
    }
}
