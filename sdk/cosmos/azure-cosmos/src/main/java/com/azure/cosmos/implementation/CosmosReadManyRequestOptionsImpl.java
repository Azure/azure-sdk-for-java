// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

public class CosmosReadManyRequestOptionsImpl extends CosmosQueryRequestOptionsBase<CosmosReadManyRequestOptionsImpl> {
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
