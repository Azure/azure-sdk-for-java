// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

public enum OperationName {
    EVENT("event"),
    PROCESS("process"),
    SEND("send"),
    RECEIVE("receive"),
    CHECKPOINT("checkpoint"),
    GET_EVENT_HUB_PROPERTIES("get_event_hub_properties"),
    GET_PARTITION_PROPERTIES("get_partition_properties");

    private final String value;
    OperationName(String operationName) {
        this.value = operationName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value;
    }
}
