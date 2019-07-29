// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos.internal.routing;

/**
 * This is the pair for Partition key and its corresponding Resource Token ,
 * this is the value in resource token map which is getting filled during the
 * construction of AsyncDocumentClient
 */
public class PartitionKeyAndResourceTokenPair {

    private PartitionKeyInternal partitionKey;
    private String resourceToken;

    public PartitionKeyAndResourceTokenPair(PartitionKeyInternal partitionKey, String resourceToken) {
        this.partitionKey = partitionKey;
        this.resourceToken = resourceToken;
    }

    /**
     * Get the Partition Key
     * 
     * @return Partition Key
     */
    public PartitionKeyInternal getPartitionKey() {
        return partitionKey;
    }

    /**
     * Sets the PartitionKey
     * 
     * @param partitionKey
     *            The Partition key
     */
    public void setPartitionKey(PartitionKeyInternal partitionKey) {
        this.partitionKey = partitionKey;
    }

    /**
     * Gets the Resource Token
     * 
     * @return Resource Token
     */
    public String getResourceToken() {
        return resourceToken;
    }

    /**
     * Sets the Resource Token
     * 
     * @param resourceToken
     *            The Resource Token
     */
    public void setResourceToken(String resourceToken) {
        this.resourceToken = resourceToken;
    }
}
