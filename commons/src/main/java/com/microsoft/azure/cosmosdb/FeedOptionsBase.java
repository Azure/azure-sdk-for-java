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

/**
 * Specifies the common options associated with feed methods (enumeration operations) in the Azure Cosmos DB database service.
 */
public abstract class FeedOptionsBase {
    private Integer maxItemCount;
    private String requestContinuation;
    private PartitionKey partitionkey;

    protected FeedOptionsBase() {}

    FeedOptionsBase(FeedOptionsBase options) {
        this.maxItemCount = options.maxItemCount;
        this.requestContinuation = options.requestContinuation;
        this.partitionkey = options.partitionkey;
    }

    /**
     * Gets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @return the max number of items.
     */
    public Integer getMaxItemCount() {
        return this.maxItemCount;
    }

    /**
     * Sets the maximum number of items to be returned in the enumeration
     * operation.
     *
     * @param maxItemCount the max number of items.
     */
    public void setMaxItemCount(Integer maxItemCount) {
        this.maxItemCount = maxItemCount;
    }

    /**
     * Gets the request continuation token.
     *
     * @return the request continuation.
     */
    public String getRequestContinuation() {
        return this.requestContinuation;
    }

    /**
     * Sets the request continuation token.
     *
     * @param requestContinuation
     *            the request continuation.
     */
    public void setRequestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
    }
    
    /**
     * Gets the partition key used to identify the current request's target
     * partition.
     *
     * @return the partition key.
     */
    public PartitionKey getPartitionKey() {
        return this.partitionkey;
    }

    /**
     * Sets the partition key used to identify the current request's target
     * partition.
     *
     * @param partitionkey
     *            the partition key value.
     */
    public void setPartitionKey(PartitionKey partitionkey) {
        this.partitionkey = partitionkey;
    }
}
