// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.Trigger;

public class CosmosAsyncTriggerResponse extends CosmosResponse<CosmosTriggerProperties> {

    private CosmosTriggerProperties cosmosTriggerProperties;
    private CosmosAsyncTrigger cosmosTrigger;

    CosmosAsyncTriggerResponse(ResourceResponse<Trigger> response, CosmosAsyncContainer container) {
        super(response);
        if(response.getResource() != null) {
            super.setProperties(new CosmosTriggerProperties(response));
            cosmosTriggerProperties = new CosmosTriggerProperties(response);
            cosmosTrigger = new CosmosAsyncTrigger(cosmosTriggerProperties.getId(), container);
        }
    }

    /**
     * Gets the cosmos trigger properties or null
     *
     * @return {@link CosmosTriggerProperties}
     */
    public CosmosTriggerProperties getProperties() {
        return cosmosTriggerProperties;
    }

    /**
     * Gets the cosmos trigger object or null
     *
     * @return {@link CosmosAsyncTrigger}
     */
    public CosmosAsyncTrigger getTrigger() {
        return cosmosTrigger;
    }
}
