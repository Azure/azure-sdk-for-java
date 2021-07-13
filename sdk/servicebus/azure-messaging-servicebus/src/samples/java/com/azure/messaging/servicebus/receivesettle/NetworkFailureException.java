// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.receivesettle;

/**
 * Used to simulate a network error during calling the order service.
 */
public class NetworkFailureException extends Exception {
    public NetworkFailureException(String message) {
        super(message);
    }
}
