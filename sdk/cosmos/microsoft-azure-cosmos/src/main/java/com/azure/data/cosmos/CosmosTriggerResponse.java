// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.Trigger;

public class CosmosTriggerResponse extends CosmosResponse<CosmosTriggerProperties> {

    private CosmosTriggerProperties cosmosTriggerProperties;
    private CosmosTrigger cosmosTrigger;

    CosmosTriggerResponse(ResourceResponse<Trigger> response, CosmosContainer container) {
        super(response);
        if(response.getResource() != null) {
            super.resourceSettings(new CosmosTriggerProperties(response));
            cosmosTriggerProperties = new CosmosTriggerProperties(response);
            cosmosTrigger = new CosmosTrigger(cosmosTriggerProperties.id(), container);
        }
    }

    /**
     * Gets the cosmos trigger properties or null
     *
     * @return {@link CosmosTriggerProperties}
     */
    public CosmosTriggerProperties properties() {
        return cosmosTriggerProperties;
    }

    /**
     * Gets the cosmos trigger object or null
     *
     * @return {@link CosmosTrigger}
     */
    public CosmosTrigger trigger() {
        return cosmosTrigger;
    }
}
