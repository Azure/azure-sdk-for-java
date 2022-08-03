// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.Locale;

public enum TagName {
    Container("Container", 1),
    Operation("Operation", 2),
    OperationStatusCode("OperationStatusCode", 4),
    ClientCorrelationId("ClientCorrelationId", 8),
    ConsistencyLevel("ConsistencyLevel", 16),
    PartitionKeyRangeId("PartitionKeyRangeId", 32),
    RequestStatusCode("RequestStatusCode", 64),
    RequestOperationType("RequestOperationType", 128),
    RegionName("RegionName", 256),
    ServiceEndpoint("ServiceEndpoint", 512),
    ServiceAddress("ServiceAddress", 1024),
    IsForceRefresh("IsForceRefresh", 2048),
    IsForceCollectionRoutingMapRefresh("IsForceCollectionRoutingMapRefresh",  4096);

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

