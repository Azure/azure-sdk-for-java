// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosTriggerProperties;
import com.azure.data.cosmos.CosmosTriggerResponse;

/**
 * The type Cosmos sync trigger response.
 */
public class CosmosSyncTriggerResponse extends CosmosSyncResponse {

    private final CosmosSyncTrigger syncTrigger;
    private final CosmosTriggerResponse asyncResponse;

    /**
     * Instantiates a new Cosmos sync trigger response.
     *
     * @param asyncResponse the async response
     * @param syncTrigger the sync trigger
     */
    CosmosSyncTriggerResponse(CosmosTriggerResponse asyncResponse,
                              CosmosSyncTrigger syncTrigger) {
        super(asyncResponse);
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
    public CosmosSyncTrigger trigger() {
        return syncTrigger;
    }
}
