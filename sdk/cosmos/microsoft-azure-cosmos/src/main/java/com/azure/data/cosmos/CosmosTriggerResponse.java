// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

/**
 * The type Cosmos sync trigger response.
 */
public class CosmosTriggerResponse extends CosmosResponse<CosmosTriggerProperties> {

    private final CosmosTrigger syncTrigger;
    private final CosmosAsyncTriggerResponse asyncResponse;

    /**
     * Instantiates a new Cosmos sync trigger response.
     *
     * @param asyncResponse the async response
     * @param syncTrigger the sync trigger
     */
    CosmosTriggerResponse(CosmosAsyncTriggerResponse asyncResponse,
                          CosmosTrigger syncTrigger) {
        super(asyncResponse.properties());
        this.asyncResponse = asyncResponse;
        this.syncTrigger = syncTrigger;
    }

    /**
     * Gets cosmos trigger properties.
     *
     * @return the cosmos trigger properties
     */
    public CosmosTriggerProperties properties() {
        return asyncResponse.properties();
    }

    /**
     * Gets cosmos sync trigger.
     *
     * @return the cosmos sync trigger
     */
    public CosmosTrigger trigger() {
        return syncTrigger;
    }
}
