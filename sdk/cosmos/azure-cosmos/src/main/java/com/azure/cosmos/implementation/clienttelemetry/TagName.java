// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.Locale;

public enum TagName {
    Container("Container", 1),
    Operation("Operation", 2),
    OperationStatusCode("OperationStatusCode", 4),
    ClientCorrelationId("ClientCorrelationId", 8),
    IsPayloadLargerThan1KB("IsPayloadLargerThan1KB", 16),
    ConsistencyLevel("ConsistencyLevel", 32),
    PartitionKeyRangeId("PartitionKeyRangeId", 64),
    RequestStatusCode("RequestStatusCode", 128),
    RequestOperationType("RequestOperationType", 256),
    RegionName("RegionName", 512),
    ServiceEndpoint("ServiceEndpoint", 1024),
    ServiceAddress("ServiceAddress", 2048),
    IsForceRefresh("IsForceRefresh", 4096),
    IsForceCollectionRoutingMapRefresh("IsForceCollectionRoutingMapRefresh",  8192);

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

