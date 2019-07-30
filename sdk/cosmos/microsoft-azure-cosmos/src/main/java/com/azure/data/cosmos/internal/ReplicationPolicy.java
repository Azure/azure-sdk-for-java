// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;

/**
 * Encapsulates the replication policy in the Azure Cosmos DB database service.
 */
public class ReplicationPolicy extends JsonSerializable {
    private static final int DEFAULT_MAX_REPLICA_SET_SIZE = 4;
    private static final int DEFAULT_MIN_REPLICA_SET_SIZE = 3;

    public ReplicationPolicy() {
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
        Integer maxReplicaSetSize = super.getInt(Constants.Properties.MAX_REPLICA_SET_SIZE);
        if (maxReplicaSetSize == null) {
            return DEFAULT_MAX_REPLICA_SET_SIZE;
        }

        return maxReplicaSetSize;
    }

    public void setMaxReplicaSetSize(int value) {
        Integer maxReplicaSetSize = super.getInt(Constants.Properties.MAX_REPLICA_SET_SIZE);
        BridgeInternal.setProperty(this, Constants.Properties.MAX_REPLICA_SET_SIZE, value);
    }

    public int getMinReplicaSetSize() {
        Integer minReplicaSetSize = super.getInt(Constants.Properties.MIN_REPLICA_SET_SIZE);
        if (minReplicaSetSize == null) {
            return DEFAULT_MIN_REPLICA_SET_SIZE;
        }

        return minReplicaSetSize;
    }
}
