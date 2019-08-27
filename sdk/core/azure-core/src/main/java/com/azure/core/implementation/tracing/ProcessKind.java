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
    SEND("send"),
    /**
     * Amqp message process call to receive data.
     */
    RECEIVE("message"),
    /**
     * Custom process call to process received messages.
     */
    PROCESS("process");

    private final String processKind;

    ProcessKind(String processKind) {
        this.processKind = processKind;
    }

    /**
     * Gets the AMQP process type.
     *
     * @return AMQP process type for the calling method.
     */
    public String getProcessKind() {
        return processKind;
    }
}
