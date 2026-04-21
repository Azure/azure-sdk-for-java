// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

public class CosmosReadManyRequestOptionsImpl extends CosmosQueryRequestOptionsBase<CosmosReadManyRequestOptionsImpl> {

    private String requestContinuation;

    /**
     * Instantiates a new read many request options.
     */
    public CosmosReadManyRequestOptionsImpl() {
        super();
    }

    /**
     * Instantiates a new read many request options.
     * @param options The request options to-be cloned
     */
    public CosmosReadManyRequestOptionsImpl(CosmosReadManyRequestOptionsImpl options) {
        super(options);
        this.requestContinuation = options.requestContinuation;
    }

    /**
     * Gets the composite continuation token for readManyByPartitionKeys.
     *
     * @return the continuation token, or null if not set.
     */
    public String getRequestContinuation() {
        return this.requestContinuation;
    }

    /**
     * Sets the composite continuation token for readManyByPartitionKeys.
     *
     * @param requestContinuation the continuation token from a previous invocation.
     * @return this instance.
     */
    public CosmosReadManyRequestOptionsImpl setRequestContinuation(String requestContinuation) {
        this.requestContinuation = requestContinuation;
        return this;
    }

    @Override
    public Boolean isContentResponseOnWriteEnabled() {
        return null;
    }

    @Override
    public Boolean getNonIdempotentWriteRetriesEnabled() {
        return null;
    }

    @Override
    public Boolean isScanInQueryEnabled() {
        return null;
    }

    @Override
    public Integer getMaxDegreeOfParallelism() {
        return null;
    }

    @Override
    public Integer getMaxBufferedItemCount() {
        return null;
    }

    @Override
    public Integer getMaxItemCount() {
        return null;
    }

    @Override
    public Integer getMaxPrefetchPageCount() {
        return null;
    }

    @Override
    public String getQueryNameOrDefault(String defaultQueryName) {
        return null;
    }
}
