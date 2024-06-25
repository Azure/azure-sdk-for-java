// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.v2.util.tracing;

/**
 * Contains constants common AMQP protocol process calls.
 *
 * @deprecated use {@link StartSpanOptions}
 */
@Deprecated
public enum ProcessKind {
    /**
     * Amqp Send Message process call to send data.
     */
    SEND,
    /**
     * Amqp message process call to receive data.
     */
    MESSAGE,
    /**
     * Custom process call to process received messages.
     */
    PROCESS
}
