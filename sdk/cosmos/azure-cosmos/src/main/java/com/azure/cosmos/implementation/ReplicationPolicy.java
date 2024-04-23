// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Encapsulates the replication policy in the Azure Cosmos DB database service.
 */
public class ReplicationPolicy extends JsonSerializable {
    private static final int DEFAULT_MAX_REPLICA_SET_SIZE = 4;
    private static final int DEFAULT_MIN_REPLICA_SET_SIZE = 3;


    /**
     * Assumption: all consistency mutations are through setDefaultConsistencyLevel only.
     * NOTE: If the underlying ObjectNode is mutated cache might be stale
     */
    private int maxReplicaSetSize;
    private int minReplicaSetSize;

    public ReplicationPolicy() {
    }

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public ReplicationPolicy(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the replication policy.
     */
    public ReplicationPolicy(String jsonString) {
        super(jsonString);
    }

    public int getMaxReplicaSetSize() {
        if (maxReplicaSetSize == 0) {
            Integer maxReplicaSetSizeFromJsonPayload = super.getInt(Constants.Properties.MAX_REPLICA_SET_SIZE);
            if (maxReplicaSetSizeFromJsonPayload == null) {
                maxReplicaSetSizeFromJsonPayload = DEFAULT_MAX_REPLICA_SET_SIZE;
            }

            maxReplicaSetSize = maxReplicaSetSizeFromJsonPayload;
        }

        return maxReplicaSetSize;
    }

    public void setMaxReplicaSetSize(int value) {
        this.set(Constants.Properties.MAX_REPLICA_SET_SIZE, value, CosmosItemSerializer.DEFAULT_SERIALIZER);
        this.maxReplicaSetSize = value;
    }

    public int getMinReplicaSetSize() {
        if (minReplicaSetSize == 0) {
            Integer minReplicaSetSizeFromJsonPayload = super.getInt(Constants.Properties.MIN_REPLICA_SET_SIZE);
            if (minReplicaSetSizeFromJsonPayload == null) {
                minReplicaSetSizeFromJsonPayload = DEFAULT_MIN_REPLICA_SET_SIZE;
            }

            minReplicaSetSize = minReplicaSetSizeFromJsonPayload;
        }

        return minReplicaSetSize;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
