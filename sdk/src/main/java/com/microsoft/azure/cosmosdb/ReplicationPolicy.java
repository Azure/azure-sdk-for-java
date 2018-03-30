/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb;

import org.json.JSONObject;

import com.microsoft.azure.cosmosdb.internal.Constants;

/**
 * Encapsulates the replication policy in the Azure Cosmos DB database service.
 */
@SuppressWarnings("serial")
public class ReplicationPolicy extends JsonSerializable {
    private static final int DEFAULT_MAX_REPLICA_SET_SIZE = 4;
    private static final int DEFAULT_MIN_REPLICA_SET_SIZE = 3;

    ReplicationPolicy() {
    }

    /**
     * Constructor.
     *
     * @param jsonString the json string that represents the replication policy.
     */
    public ReplicationPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Constructor.
     *
     * @param jsonObject the json object that represents the replication policy.
     */
    public ReplicationPolicy(JSONObject jsonObject) {
        super(jsonObject);
    }


    public int getMaxReplicaSetSize() {
        Integer maxReplicaSetSize = super.getInt(Constants.Properties.MAX_REPLICA_SET_SIZE);
        if (maxReplicaSetSize == null) {
            return DEFAULT_MAX_REPLICA_SET_SIZE;
        }

        return maxReplicaSetSize;
    }

    public int getMinReplicaSetSize() {
        Integer minReplicaSetSize = super.getInt(Constants.Properties.MIN_REPLICA_SET_SIZE);
        if (minReplicaSetSize == null) {
            return DEFAULT_MIN_REPLICA_SET_SIZE;
        }

        return minReplicaSetSize;
    }
}
