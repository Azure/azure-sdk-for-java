// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

public class FaultInjectionEndpoints {
    private static final int DEFAULT_FAULT_INJECTION_REPLICA_COUNT = Integer.MAX_VALUE;
    private static final boolean DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY = false;

    private final PartitionKey partitionKey;
    private boolean includePrimary;
    private int replicaCount;

    public FaultInjectionEndpoints(PartitionKey partitionKey) {
        this(partitionKey, DEFAULT_FAULT_INJECTION_REPLICA_COUNT, DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY);
    }

    public FaultInjectionEndpoints(PartitionKey partitionKey, int replicaCount) {
        this(partitionKey, replicaCount, DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY);
    }

    public FaultInjectionEndpoints(PartitionKey partitionKey, int replicaCount, boolean includePrimary) {
        this.partitionKey = partitionKey;
        this.replicaCount = replicaCount;
        this.includePrimary = includePrimary;
    }

    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    public boolean isIncludePrimary() {
        return includePrimary;
    }

    public int getReplicaCount() {
        return replicaCount;
    }
}
