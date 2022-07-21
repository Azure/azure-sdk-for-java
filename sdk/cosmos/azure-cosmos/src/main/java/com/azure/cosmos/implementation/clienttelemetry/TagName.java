// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.Locale;

public enum TagName {
    ClientCorrelationId("ClientCorrelationId", 1),
    Account("Account", 2),
    Database("Database", 4),
    Container("Container", 8),
    ResourceType("ResourceType", 16),
    OperationType("OperationType", 32),
    StatusCode("StatusCode", 64),
    OperationId("OperationId", 128),
    RegionsContacted("RegionsContacted", 256),
    IsPayloadLargerThan1KB("IsPayloadLargerThan1KB", 512),
    ConsistencyLevel("ConsistencyLevel", 1024);

    private final int value;
    private final String stringValue;
    private final String toLowerStringValue;

    TagName(String stringValue, int value) {
        this.stringValue = stringValue;
        this.value = value;
        this.toLowerStringValue = stringValue.toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return this.stringValue;
    }

    public String toLowerCase() {
        return this.toLowerStringValue;
    }

    public int value() {
        return this.value;
    }
}

