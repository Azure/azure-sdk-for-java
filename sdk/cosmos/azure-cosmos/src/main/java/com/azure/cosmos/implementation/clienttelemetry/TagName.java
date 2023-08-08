// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.clienttelemetry;

import java.util.EnumSet;
import java.util.Locale;

public enum TagName {
    Container("Container", 1 << 0),
    Operation("Operation", 1 << 1),
    OperationStatusCode("OperationStatusCode", 1 << 2),
    ClientCorrelationId("ClientCorrelationId", 1 << 3),
    ConsistencyLevel("ConsistencyLevel", 1 << 4),
    PartitionKeyRangeId("PartitionKeyRangeId", 1 << 5),
    RequestStatusCode("RequestStatusCode", 1 << 6),
    RequestOperationType("RequestOperationType", 1 << 7),
    RegionName("RegionName", 1 << 8),
    ServiceEndpoint("ServiceEndpoint", 1 << 9),
    ServiceAddress("ServiceAddress", 1 << 10),
    IsForceRefresh("IsForceRefresh", 1 << 11),
    IsForceCollectionRoutingMapRefresh("IsForceCollectionRoutingMapRefresh",  1 << 12),
    PartitionId("PartitionId", 1 << 13),
    ReplicaId("ReplicaId", 1 << 14),
    OperationSubStatusCode("OperationSubStatusCode", 1 << 15);

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

    public static final EnumSet<TagName> DEFAULT_TAGS = EnumSet.of(
        TagName.Container,
        TagName.Operation,
        TagName.OperationStatusCode,
        TagName.ClientCorrelationId,
        TagName.RequestStatusCode,
        TagName.RequestOperationType,
        TagName.ServiceAddress,
        TagName.RegionName
    );

    public static final EnumSet<TagName> ALL_TAGS = EnumSet.allOf(TagName.class);

    public static final EnumSet<TagName> MINIMUM_TAGS = EnumSet.of(
        TagName.Container,
        TagName.Operation,
        TagName.OperationStatusCode,
        TagName.ClientCorrelationId,
        TagName.RequestStatusCode,
        TagName.RequestOperationType
    );
}

