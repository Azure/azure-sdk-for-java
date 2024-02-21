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
}
