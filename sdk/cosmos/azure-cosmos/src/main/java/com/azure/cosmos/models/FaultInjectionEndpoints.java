package com.azure.cosmos.models;

public class FaultInjectionEndpoints {
    private static final int DEFAULT_FAULT_INJECTION_REPLICA_COUNT = Integer.MAX_VALUE;
    private static final boolean DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY = false;

    private final PartitionKey partitionKey;
    private boolean excludePrimary;
    private int replicaCount;

    public FaultInjectionEndpoints(PartitionKey partitionKey) {
        this(partitionKey, DEFAULT_FAULT_INJECTION_REPLICA_COUNT, DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY);
    }

    public FaultInjectionEndpoints(PartitionKey partitionKey, int replicaCount) {
        this(partitionKey, replicaCount, DEFAULT_FAULT_INJECTION_EXCLUDE_PRIMARY);
    }

    public FaultInjectionEndpoints(PartitionKey partitionKey, int replicaCount, boolean excludePrimary) {
        this.partitionKey = partitionKey;
        this.replicaCount = replicaCount;
        this.excludePrimary = excludePrimary;
    }

    public PartitionKey getPartitionKey() {
        return partitionKey;
    }

    public boolean isExcludePrimary() {
        return excludePrimary;
    }

    public int getReplicaCount() {
        return replicaCount;
    }
}
