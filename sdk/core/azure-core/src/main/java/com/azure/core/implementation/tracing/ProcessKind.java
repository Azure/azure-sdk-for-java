// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.tracing;

/**
 * Contains constants common AMQP protocol process calls.
 *
 */
public enum ProcessKind {
    /**
     * Eventhubs send process call.
     */
    SEND("send"),
    /**
     * Eventhubs message process call.
     */
    MESSAGE("message"),
    /**
     * Event Processor host Process process call.
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
