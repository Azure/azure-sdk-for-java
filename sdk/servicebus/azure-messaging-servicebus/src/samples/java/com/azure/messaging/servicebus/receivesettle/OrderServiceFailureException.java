// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.receivesettle;

/**
 * The exception that the order service throws during processing an order or orders.
 */
public class OrderServiceFailureException extends Exception {
    public OrderServiceFailureException(String message) {
        super(message);
    }
}
