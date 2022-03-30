// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum ServiceItemLeaseVersion {
    PartitionKeyRangeBasedLease(0),
    EPKRangeBasedLease(1);

    private final int versionId;

    private static final Map<Integer, ServiceItemLeaseVersion> versionMap;

    static {
        versionMap = new ConcurrentHashMap<>();
        Arrays.stream(values()).forEach(value -> versionMap.put(value.versionId, value));
    }

    public static Optional<ServiceItemLeaseVersion> valueOf(final int version) {
        return Optional.ofNullable(versionMap.get(version));
    }

    ServiceItemLeaseVersion(int versionId) {
        this.versionId = versionId;
    }

    @JsonValue
    public int getVersionId() {
        return this.versionId;
    }
}
