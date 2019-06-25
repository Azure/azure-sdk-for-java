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
package com.microsoft.azure.cosmosdb.internal.routing;

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
