// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.changefeed.implementation.leaseManagement;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ServiceItemLeaseVersion {
    PartitionKeyRangeBasedLease(0),
    EPKRangeBasedLease(1);

    private final int value;

    ServiceItemLeaseVersion(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return this.value;
    }
}
