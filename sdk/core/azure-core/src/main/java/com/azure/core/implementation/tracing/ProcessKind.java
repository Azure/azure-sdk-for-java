// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.tracing;

/**
 * Contains constants common AMQP protocol process calls.
 *
 */
public enum ProcessKind {
    /**
     * Amqp Send Message process call to send data.
     */
    SEND,
    /**
     * Amqp message process call to receive data.
     */
    RECEIVE,
    /**
     * Custom process call to process received messages.
     */
    PROCESS
}
